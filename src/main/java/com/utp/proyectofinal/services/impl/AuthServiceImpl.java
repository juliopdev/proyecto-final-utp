package com.utp.proyectofinal.services.impl;

import com.utp.proyectofinal.models.documents.UserDocument;
import com.utp.proyectofinal.models.dto.AuthResponse;
import com.utp.proyectofinal.models.dto.LoginRequest;
import com.utp.proyectofinal.models.dto.RegisterRequest;
import com.utp.proyectofinal.models.entities.Role;
import com.utp.proyectofinal.models.entities.User;
import com.utp.proyectofinal.repositories.mongodb.LogDocumentRepository;
import com.utp.proyectofinal.repositories.mongodb.UserDocumentRepository;
import com.utp.proyectofinal.repositories.postgresql.RoleRepository;
import com.utp.proyectofinal.repositories.postgresql.UserRepository;
import com.utp.security.JwtConfig;
import com.utp.proyectofinal.services.interfaces.AuthService;
import com.utp.proyectofinal.services.interfaces.EmailService;
import com.utp.proyectofinal.services.interfaces.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del servicio de autenticación
 * 
 * PATRONES IMPLEMENTADOS:
 * - FACADE: Fachada que coordina múltiples servicios y repositorios
 * - STRATEGY: Diferentes estrategias de autenticación y validación
 * - COMMAND: Cada método encapsula comandos complejos
 * - TEMPLATE METHOD: Templates para procesos de autenticación
 * - FACTORY: Factory methods para crear usuarios y tokens
 * - OBSERVER: Notifica eventos a través del LogService
 * 
 * @author Julio Pariona
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final RoleRepository roleRepository;
    private final LogDocumentRepository logRepository;
    
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final UserDetailsService userDetailsService;
    
    private final LogService logService;
    private final EmailService emailService;

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration:30}")
    private int lockoutDurationMinutes;

    /**
     * PATRÓN FACADE: Orquesta el proceso completo de registro
     * PATRÓN FACTORY: Crea nuevo usuario
     */
    @Override
    public User registerUser(RegisterRequest registerRequest, String ipAddress) {
        log.info("Iniciando registro de usuario: {}", registerRequest.getEmail());

        // PATRÓN STRATEGY: Validaciones de negocio
        validateRegistrationRequest(registerRequest);

        try {
            // PATRÓN FACTORY: Crear usuario en PostgreSQL
            User user = createUserFromRequest(registerRequest);
            User savedUser = userRepository.save(user);

            // PATRÓN FACTORY: Crear documento en MongoDB
            UserDocument userDocument = createUserDocumentFromRequest(registerRequest, savedUser.getId());
            UserDocument savedDocument = userDocumentRepository.save(userDocument);

            // Sincronizar IDs entre bases de datos
            savedUser.setMongoUserId(savedDocument.getId());
            userRepository.save(savedUser);

            // PATRÓN COMMAND: Generar y enviar token de verificación
            String verificationToken = generateVerificationToken(savedUser.getId(), "EMAIL_VERIFICATION");
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), verificationToken);

            // PATRÓN OBSERVER: Registrar evento
            logService.logUserRegistration(savedUser.getEmail(), ipAddress);

            log.info("Usuario registrado exitosamente: {}", savedUser.getEmail());
            return savedUser;

        } catch (Exception e) {
            log.error("Error en registro de usuario {}: {}", registerRequest.getEmail(), e.getMessage());
            // PATRÓN OBSERVER: Registrar error
            logService.logRegistrationError(registerRequest.getEmail(), e.getMessage(), ipAddress);
            throw new RuntimeException("Error en el registro: " + e.getMessage(), e);
        }
    }

    /**
     * PATRÓN FACADE: Orquesta el proceso de autenticación para API
     */
    @Override
    public AuthResponse authenticateUser(LoginRequest loginRequest, String ipAddress) {
        log.debug("Iniciando autenticación API para: {}", loginRequest.getEmail());

        try {
            // PATRÓN STRATEGY: Validaciones de seguridad
            validateLoginAttempt(loginRequest.getEmail(), ipAddress);

            // PATRÓN COMMAND: Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmailAndDeletedAtIsNull(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

            // PATRÓN FACTORY: Generar token JWT
            String token = jwtConfig.generateToken(userDetails);

            // PATRÓN COMMAND: Actualizar último login
            updateLastLogin(user.getId(), ipAddress);

            // PATRÓN OBSERVER: Registrar login exitoso
            logService.logUserLogin(user.getEmail(), ipAddress, "API", true);

            // PATRÓN FACTORY: Crear respuesta
            return AuthResponse.success(
                token, 
                user.getId(), 
                user.getEmail(), 
                user.getFullName(), 
                user.getRole().getName().name()
            );

        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para: {}", loginRequest.getEmail());
            registerFailedLoginAttempt(loginRequest.getEmail(), ipAddress, "Credenciales inválidas");
            throw new RuntimeException("Email o contraseña incorrectos");
        }
    }

    /**
     * PATRÓN FACADE: Autenticación para web (SSR)
     */
    @Override
    public User authenticateUserWeb(String email, String password, String ipAddress) {
        log.debug("Iniciando autenticación Web para: {}", email);

        // PATRÓN STRATEGY: Validaciones
        validateLoginAttempt(email, ipAddress);

        Optional<User> userOpt = userRepository.findByEmailAndDeletedAtIsNull(email);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            registerFailedLoginAttempt(email, ipAddress, "Credenciales inválidas");
            throw new RuntimeException("Email o contraseña incorrectos");
        }

        User user = userOpt.get();

        // PATRÓN STRATEGY: Validaciones de estado
        if (!user.isEnabled()) {
            throw new RuntimeException("Cuenta deshabilitada");
        }
        if (user.isLocked()) {
            throw new RuntimeException("Cuenta bloqueada");
        }

        // PATRÓN COMMAND: Actualizar último login
        updateLastLogin(user.getId(), ipAddress);

        // PATRÓN OBSERVER: Registrar login exitoso
        logService.logUserLogin(user.getEmail(), ipAddress, "WEB", true);

        return user;
    }

    /**
     * PATRÓN COMMAND: Verificar email del usuario
     */
    @Override
    public void verifyEmail(String token) {
        log.debug("Verificando email con token");

        // Aquí implementarías la lógica de verificación de token
        // Por simplicidad, asumo que tienes una tabla de tokens
        
        // PATRÓN STRATEGY: Validar token
        if (!isValidVerificationToken(token)) {
            throw new RuntimeException("Token de verificación inválido o expirado");
        }

        // Extraer usuario del token y marcar como verificado
        Long userId = extractUserIdFromToken(token);
        userRepository.updateVerificationStatus(userId, true);

        // Actualizar en MongoDB también
        Optional<UserDocument> userDoc = userDocumentRepository.findByPostgresUserId(userId);
        if (userDoc.isPresent()) {
            UserDocument doc = userDoc.get();
            doc.setIsVerified(true);
            doc.setStatus("ACTIVE");
            userDocumentRepository.save(doc);
        }

        // Marcar token como usado
        markTokenAsUsed(token);

        // PATRÓN OBSERVER: Registrar verificación
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            logService.logEmailVerification(user.getEmail());
        }

        log.info("Email verificado exitosamente para usuario ID: {}", userId);
    }

    /**
     * PATRÓN FACADE: Iniciar proceso de recuperación de contraseña
     */
    @Override
    public void initiatePasswordReset(String email, String ipAddress) {
        log.debug("Iniciando recuperación de contraseña para: {}", email);

        Optional<User> userOpt = userRepository.findByEmailAndDeletedAtIsNull(email);
        if (userOpt.isEmpty()) {
            // Por seguridad, no revelamos si el email existe
            log.warn("Intento de recuperación para email inexistente: {}", email);
            return;
        }

        User user = userOpt.get();

        // PATRÓN FACTORY: Generar token de reset
        String resetToken = generateVerificationToken(user.getId(), "PASSWORD_RESET");

        // PATRÓN COMMAND: Enviar email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);

        // PATRÓN OBSERVER: Registrar evento
        logService.logPasswordResetRequest(user.getEmail(), ipAddress);

        log.info("Email de recuperación enviado a: {}", email);
    }

    /**
     * PATRÓN COMMAND: Resetear contraseña con token
     */
    @Override
    public String resetPassword(String token, String newPassword) {
        log.debug("Reseteando contraseña con token");

        // PATRÓN STRATEGY: Validar token
        if (!isValidResetToken(token)) {
            throw new RuntimeException("Token de reset inválido o expirado");
        }

        Long userId = extractUserIdFromToken(token);
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // PATRÓN COMMAND: Actualizar contraseña
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Marcar token como usado
        markTokenAsUsed(token);

        // PATRÓN OBSERVER: Registrar reset
        logService.logPasswordReset(user.getEmail(), "TOKEN_RESET");

        log.info("Contraseña reseteada exitosamente para: {}", user.getEmail());
        return user.getEmail();
    }

    /**
     * PATRÓN STRATEGY: Validar token de reset
     */
    @Override
    public boolean isValidResetToken(String token) {
        // Implementar validación de token
        // Por ahora retorna true como placeholder
        return isValidToken(token, "PASSWORD_RESET");
    }

    /**
     * PATRÓN COMMAND: Cambiar contraseña con validación de contraseña actual
     */
    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword, String ipAddress) {
        log.debug("Cambiando contraseña para usuario ID: {}", userId);

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // PATRÓN STRATEGY: Validar contraseña actual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logService.logPasswordChangeAttempt(user.getEmail(), ipAddress, false);
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        // PATRÓN COMMAND: Actualizar contraseña
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // PATRÓN OBSERVER: Registrar cambio
        logService.logPasswordChangeAttempt(user.getEmail(), ipAddress, true);

        log.info("Contraseña cambiada exitosamente para: {}", user.getEmail());
    }

    /**
     * PATRÓN COMMAND: Logout del usuario
     */
    @Override
    public void logout(Long userId, String ipAddress) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
        if (user != null) {
            // PATRÓN OBSERVER: Registrar logout
            logService.logUserLogout(user.getEmail(), ipAddress);
            log.debug("Usuario {} ha cerrado sesión", user.getEmail());
        }
    }

    /**
     * PATRÓN STRATEGY: Validar token JWT
     */
    @Override
    public boolean isValidJwtToken(String token) {
        try {
            String username = jwtConfig.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtConfig.isTokenValid(token, userDetails);
        } catch (Exception e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * PATRÓN FACTORY: Refrescar token JWT
     */
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // Implementar lógica de refresh token
        // Por ahora como placeholder
        throw new RuntimeException("Refresh token no implementado aún");
    }

    /**
     * PATRÓN COMMAND: Bloquear usuario
     */
    @Override
    public void lockUser(Long userId, String reason, Long adminId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setLocked(true);
        userRepository.save(user);

        // PATRÓN OBSERVER: Registrar bloqueo
        User admin = userRepository.findById(adminId).orElse(null);
        String adminEmail = admin != null ? admin.getEmail() : "Sistema";
        logService.logUserLocked(user.getEmail(), reason, adminEmail);

        log.info("Usuario {} bloqueado por: {}", user.getEmail(), adminEmail);
    }

    /**
     * PATRÓN COMMAND: Desbloquear usuario
     */
    @Override
    public void unlockUser(Long userId, Long adminId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setLocked(false);
        userRepository.save(user);

        // PATRÓN OBSERVER: Registrar desbloqueo
        User admin = userRepository.findById(adminId).orElse(null);
        String adminEmail = admin != null ? admin.getEmail() : "Sistema";
        logService.logUserUnlocked(user.getEmail(), adminEmail);

        log.info("Usuario {} desbloqueado por: {}", user.getEmail(), adminEmail);
    }

    // Métodos privados de apoyo...

    /**
     * PATRÓN STRATEGY: Validar request de registro
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (request.getPhone() != null && userRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())) {
            throw new RuntimeException("El teléfono ya está registrado");
        }
    }

    /**
     * PATRÓN FACTORY: Crear usuario desde request
     */
    private User createUserFromRequest(RegisterRequest request) {
        Role defaultRole = roleRepository.findByName(Role.RoleName.USER)
            .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

        return User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .birthDate(request.getBirthDate())
            .address(request.getAddress())
            .role(defaultRole)
            .enabled(true)
            .locked(false)
            .verified(false)
            .build();
    }

    /**
     * PATRÓN FACTORY: Crear documento de usuario desde request
     */
    private UserDocument createUserDocumentFromRequest(RegisterRequest request, Long postgresUserId) {
        return UserDocument.builder()
            .username(request.getEmail()) // Usar email como username por defecto
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhone())
            .status("PENDING_VERIFICATION")
            .isVerified(false)
            .postgresUserId(postgresUserId)
            .registrationDate(LocalDateTime.now())
            .notificationPreferences(UserDocument.NotificationPreferences.builder()
                .emailAlerts(true)
                .inAppNotifications(true)
                .marketingEmails(request.getAcceptMarketing())
                .orderUpdates(true)
                .build())
            .build();
    }

    // Métodos placeholder para implementar según necesidades específicas
    
    private void validateLoginAttempt(String email, String ipAddress) {
        // Implementar validaciones de intentos fallidos, IP bloqueada, etc.
    }

    @Override
    public boolean validateCredentials(String email, String password) {
        return false; // Implementar
    }

    @Override
    public void updateLastLogin(Long userId, String ipAddress) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
        // También actualizar en MongoDB si es necesario
    }

    @Override
    public String generateVerificationToken(Long userId, String tokenType) {
        // Implementar generación de token y almacenamiento en BD
        return UUID.randomUUID().toString();
    }

    @Override
    public void resendVerificationEmail(String email, String ipAddress) {
        // Implementar reenvío de email de verificación
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public boolean isUserLocked(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
            .map(User::isLocked)
            .orElse(false);
    }

    @Override
    public boolean isUserVerified(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
            .map(User::isVerified)
            .orElse(false);
    }

    @Override
    public void registerFailedLoginAttempt(String email, String ipAddress, String reason) {
        logService.logFailedLogin(email, ipAddress, reason);
    }

    @Override
    public boolean isIpBlocked(String ipAddress) {
        // Implementar lógica de bloqueo por IP
        return false;
    }

    @Override
    public void cleanupExpiredTokens() {
        // Implementar limpieza de tokens expirados
    }

    // Métodos auxiliares privados
    private boolean isValidVerificationToken(String token) { return true; }
    private boolean isValidToken(String token, String type) { return true; }
    private Long extractUserIdFromToken(String token) { return 1L; }
    private void markTokenAsUsed(String token) { }
}