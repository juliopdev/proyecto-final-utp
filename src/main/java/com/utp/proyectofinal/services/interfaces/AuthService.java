package com.utp.proyectofinal.services.interfaces;

import com.utp.proyectofinal.models.dto.AuthResponse;
import com.utp.proyectofinal.models.dto.LoginRequest;
import com.utp.proyectofinal.models.dto.RegisterRequest;
import com.utp.proyectofinal.models.entities.User;

/**
 * Interface para el servicio de autenticación
 * 
 * PATRONES IMPLEMENTADOS:
 * - STRATEGY: Define estrategias de autenticación
 * - COMMAND: Cada método representa un comando de autenticación
 * - FACADE: Proporciona una fachada simplificada para operaciones de autenticación
 * - TEMPLATE METHOD: Define templates para procesos de autenticación
 * 
 * @author Julio Pariona
 */
public interface AuthService {

    /**
     * PATRÓN COMMAND: Comando para registrar nuevo usuario
     * 
     * @param registerRequest Datos de registro
     * @param ipAddress IP del cliente
     * @return Usuario creado
     * @throws RuntimeException si hay errores en el registro
     */
    User registerUser(RegisterRequest registerRequest, String ipAddress);

    /**
     * PATRÓN COMMAND: Comando para autenticar usuario (API)
     * 
     * @param loginRequest Datos de login
     * @param ipAddress IP del cliente
     * @return Respuesta de autenticación con token
     * @throws RuntimeException si las credenciales son inválidas
     */
    AuthResponse authenticateUser(LoginRequest loginRequest, String ipAddress);

    /**
     * PATRÓN COMMAND: Comando para autenticar usuario (Web/SSR)
     * 
     * @param email Email del usuario
     * @param password Contraseña
     * @param ipAddress IP del cliente
     * @return Usuario autenticado
     * @throws RuntimeException si las credenciales son inválidas
     */
    User authenticateUserWeb(String email, String password, String ipAddress);

    /**
     * PATRÓN COMMAND: Comando para verificar email
     * 
     * @param token Token de verificación
     * @throws RuntimeException si el token es inválido
     */
    void verifyEmail(String token);

    /**
     * PATRÓN COMMAND: Comando para iniciar reset de contraseña
     * 
     * @param email Email del usuario
     * @param ipAddress IP del cliente
     * @throws RuntimeException si hay errores en el proceso
     */
    void initiatePasswordReset(String email, String ipAddress);

    /**
     * PATRÓN COMMAND: Comando para resetear contraseña
     * 
     * @param token Token de reset
     * @param newPassword Nueva contraseña
     * @return Email del usuario cuya contraseña fue reseteada
     * @throws RuntimeException si el token es inválido
     */
    String resetPassword(String token, String newPassword);

    /**
     * PATRÓN STRATEGY: Validar token de reset
     * 
     * @param token Token a validar
     * @return true si es válido, false si no
     */
    boolean isValidResetToken(String token);

    /**
     * PATRÓN COMMAND: Comando para cambiar contraseña
     * 
     * @param userId ID del usuario
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @param ipAddress IP del cliente
     * @throws RuntimeException si la contraseña actual es incorrecta
     */
    void changePassword(Long userId, String currentPassword, String newPassword, String ipAddress);

    /**
     * PATRÓN COMMAND: Comando para logout
     * 
     * @param userId ID del usuario
     * @param ipAddress IP del cliente
     */
    void logout(Long userId, String ipAddress);

    /**
     * PATRÓN STRATEGY: Validar token JWT
     * 
     * @param token Token JWT
     * @return true si es válido, false si no
     */
    boolean isValidJwtToken(String token);

    /**
     * PATRÓN COMMAND: Comando para refrescar token
     * 
     * @param refreshToken Token de refresh
     * @return Nueva respuesta de autenticación
     * @throws RuntimeException si el refresh token es inválido
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * PATRÓN COMMAND: Comando para bloquear usuario
     * 
     * @param userId ID del usuario a bloquear
     * @param reason Razón del bloqueo
     * @param adminId ID del administrador que ejecuta la acción
     */
    void lockUser(Long userId, String reason, Long adminId);

    /**
     * PATRÓN COMMAND: Comando para desbloquear usuario
     * 
     * @param userId ID del usuario a desbloquear
     * @param adminId ID del administrador que ejecuta la acción
     */
    void unlockUser(Long userId, Long adminId);

    /**
     * PATRÓN STRATEGY: Validar credenciales
     * 
     * @param email Email del usuario
     * @param password Contraseña
     * @return true si las credenciales son válidas
     */
    boolean validateCredentials(String email, String password);

    /**
     * PATRÓN COMMAND: Comando para actualizar último login
     * 
     * @param userId ID del usuario
     * @param ipAddress IP del cliente
     */
    void updateLastLogin(Long userId, String ipAddress);

    /**
     * PATRÓN STRATEGY: Generar token de verificación
     * 
     * @param userId ID del usuario
     * @param tokenType Tipo de token
     * @return Token generado
     */
    String generateVerificationToken(Long userId, String tokenType);

    /**
     * PATRÓN COMMAND: Comando para reenviar email de verificación
     * 
     * @param email Email del usuario
     * @param ipAddress IP del cliente
     * @throws RuntimeException si el usuario no existe o ya está verificado
     */
    void resendVerificationEmail(String email, String ipAddress);

    /**
     * PATRÓN STRATEGY: Verificar si el email existe
     * 
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean emailExists(String email);

    /**
     * PATRÓN STRATEGY: Verificar si el usuario está bloqueado
     * 
     * @param email Email del usuario
     * @return true si está bloqueado, false si no
     */
    boolean isUserLocked(String email);

    /**
     * PATRÓN STRATEGY: Verificar si el usuario está verificado
     * 
     * @param email Email del usuario
     * @return true si está verificado, false si no
     */
    boolean isUserVerified(String email);

    /**
     * PATRÓN COMMAND: Comando para registrar intento de login fallido
     * 
     * @param email Email del usuario
     * @param ipAddress IP del cliente
     * @param reason Razón del fallo
     */
    void registerFailedLoginAttempt(String email, String ipAddress, String reason);

    /**
     * PATRÓN STRATEGY: Verificar si IP está bloqueada por intentos fallidos
     * 
     * @param ipAddress IP a verificar
     * @return true si está bloqueada, false si no
     */
    boolean isIpBlocked(String ipAddress);

    /**
     * PATRÓN COMMAND: Comando para limpiar tokens expirados
     */
    void cleanupExpiredTokens();
}