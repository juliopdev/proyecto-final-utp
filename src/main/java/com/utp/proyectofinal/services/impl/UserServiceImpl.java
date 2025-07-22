package com.utp.proyectofinal.services.impl;

import com.utp.proyectofinal.models.documents.UserDocument;
import com.utp.proyectofinal.models.dto.UpdateProfileRequest;
import com.utp.proyectofinal.models.dto.UserProfileResponse;
import com.utp.proyectofinal.models.entities.Role;
import com.utp.proyectofinal.models.entities.User;
import com.utp.proyectofinal.repositories.mongodb.UserDocumentRepository;
import com.utp.proyectofinal.repositories.postgresql.RoleRepository;
import com.utp.proyectofinal.repositories.postgresql.UserRepository;
import com.utp.proyectofinal.services.interfaces.LogService;
import com.utp.proyectofinal.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de usuarios
 * 
 * PATRONES IMPLEMENTADOS:
 * - FACADE: Coordina operaciones entre PostgreSQL y MongoDB
 * - STRATEGY: Diferentes estrategias de búsqueda, filtrado y validación
 * - COMMAND: Cada método encapsula comandos complejos de gestión
 * - ADAPTER: Adapta entre User entities y UserDocument
 * - OBSERVER: Notifica eventos a través del LogService
 * - TEMPLATE METHOD: Templates para operaciones CRUD
 * 
 * @author Julio Pariona
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserDocumentRepository userDocumentRepository;
  private final RoleRepository roleRepository;
  private final LogService logService;

  // ===== OPERACIONES BÁSICAS DE USUARIO =====

  /**
   * PATRÓN COMMAND: Obtener usuario por ID
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(Long id) {
    log.debug("Buscando usuario por ID: {}", id);
    return userRepository.findByIdAndDeletedAtIsNull(id);
  }

  /**
   * PATRÓN COMMAND: Obtener usuario por email
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    log.debug("Buscando usuario por email: {}", email);
    return userRepository.findByEmailAndDeletedAtIsNull(email);
  }

  /**
   * PATRÓN COMMAND: Obtener documento de usuario por email
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<UserDocument> findUserDocumentByEmail(String email) {
    log.debug("Buscando documento de usuario por email: {}", email);
    return userDocumentRepository.findByEmail(email);
  }

  /**
   * PATRÓN FACADE: Obtener perfil completo combinando PostgreSQL y MongoDB
   */
  @Override
  @Transactional(readOnly = true)
  public UserProfileResponse getUserProfile(Long userId) {
    log.debug("Obteniendo perfil completo para usuario ID: {}", userId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // PATRÓN ADAPTER: Convertir User a UserProfileResponse
    UserProfileResponse.UserProfileResponseBuilder responseBuilder = UserProfileResponse.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .phone(user.getPhone())
        .birthDate(user.getBirthDate())
        .address(user.getAddress())
        .enabled(user.getEnabled())
        .locked(user.getLocked())
        .verified(user.getVerified())
        .roleName(user.getRole().getName().name())
        .roleDisplayName(user.getRole().getName().getDisplayName())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .lastLogin(user.getLastLogin());

    // Obtener datos adicionales de MongoDB si existe
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();

        // PATRÓN ADAPTER: Adaptar datos de MongoDB
        responseBuilder
            .username(doc.getUsername())
            .status(doc.getStatus())
            .registrationDate(doc.getRegistrationDate());

        // Adaptar dirección detallada
        if (doc.getAddress() != null) {
          responseBuilder.addressDetails(UserProfileResponse.AddressDetails.builder()
              .street(doc.getAddress().getStreet())
              .city(doc.getAddress().getCity())
              .state(doc.getAddress().getState())
              .zipCode(doc.getAddress().getZipCode())
              .country(doc.getAddress().getCountry())
              .build());
        }

        // Adaptar preferencias de notificación
        if (doc.getNotificationPreferences() != null) {
          UserDocument.NotificationPreferences prefs = doc.getNotificationPreferences();
          responseBuilder.notificationPreferences(UserProfileResponse.NotificationPreferences.builder()
              .emailAlerts(prefs.getEmailAlerts())
              .inAppNotifications(prefs.getInAppNotifications())
              .marketingEmails(prefs.getMarketingEmails())
              .orderUpdates(prefs.getOrderUpdates())
              .build());
        }

        responseBuilder
            .privacySettings(doc.getMetadata())
            .metadata(doc.getMetadata());
      }
    }

    // Obtener estadísticas del usuario
    UserProfileResponse.UserStatistics statistics = buildUserStatistics(userId);
    responseBuilder.statistics(statistics);

    UserProfileResponse response = responseBuilder.build();
    log.debug("Perfil obtenido exitosamente para usuario: {}", user.getEmail());

    return response;
  }

  /**
   * PATRÓN FACADE: Actualizar perfil coordinando PostgreSQL y MongoDB
   */
  @Override
  public User updateUserProfile(Long userId, UpdateProfileRequest updateRequest, String ipAddress) {
    log.debug("Actualizando perfil para usuario ID: {}", userId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // PATRÓN STRATEGY: Validar datos únicos
    validateUniqueFields(updateRequest, userId);

    // Actualizar datos en PostgreSQL
    user.setFirstName(updateRequest.getFirstName());
    user.setLastName(updateRequest.getLastName());
    user.setPhone(updateRequest.getPhone());
    user.setBirthDate(updateRequest.getBirthDate());
    user.setAddress(updateRequest.getAddress());

    User updatedUser = userRepository.save(user);

    // Actualizar datos en MongoDB si existe
    if (user.getMongoUserId() != null) {
      updateUserDocumentFromRequest(user.getMongoUserId(), updateRequest);
    }

    // PATRÓN OBSERVER: Registrar actualización
    logService.logUserUpdate(user.getEmail(), ipAddress);

    log.info("Perfil actualizado exitosamente para usuario: {}", user.getEmail());
    return updatedUser;
  }

  /**
   * PATRÓN COMMAND: Eliminar usuario con soft delete
   */
  @Override
  public void deleteUser(Long userId, String reason, Long adminId) {
    log.debug("Eliminando usuario ID: {} por admin ID: {}", userId, adminId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // PATRÓN COMMAND: Soft delete
    user.markAsDeleted();
    userRepository.save(user);

    // Actualizar estado en MongoDB
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();
        doc.setStatus("DELETED");
        doc.addMetadata("deletedReason", reason);
        doc.addMetadata("deletedAt", LocalDateTime.now());
        userDocumentRepository.save(doc);
      }
    }

    // PATRÓN OBSERVER: Registrar eliminación
    User admin = userRepository.findById(adminId).orElse(null);
    String adminEmail = admin != null ? admin.getEmail() : "Sistema";
    logService.logUserDeleted(user.getEmail(), reason, adminEmail);

    log.warn("Usuario eliminado: {} por {}, razón: {}", user.getEmail(), adminEmail, reason);
  }

  /**
   * PATRÓN COMMAND: Restaurar usuario eliminado
   */
  @Override
  public void restoreUser(Long userId, Long adminId) {
    log.debug("Restaurando usuario ID: {} por admin ID: {}", userId, adminId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if (user.getDeletedAt() == null) {
      throw new RuntimeException("El usuario no está eliminado");
    }

    // Restaurar usuario
    user.setDeletedAt(null);
    user.setEnabled(true);
    userRepository.save(user);

    // Actualizar estado en MongoDB
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();
        doc.setStatus("ACTIVE");
        doc.getMetadata().remove("deletedReason");
        doc.getMetadata().remove("deletedAt");
        userDocumentRepository.save(doc);
      }
    }

    // PATRÓN OBSERVER: Registrar restauración
    User admin = userRepository.findById(adminId).orElse(null);
    String adminEmail = admin != null ? admin.getEmail() : "Sistema";
    logService.logUserRestored(user.getEmail(), adminEmail);

    log.info("Usuario restaurado: {} por {}", user.getEmail(), adminEmail);
  }

  // ===== OPERACIONES DE ADMINISTRACIÓN =====

  /**
   * PATRÓN STRATEGY: Obtener usuarios con paginación
   */
  @Override
  @Transactional(readOnly = true)
  public Page<User> getAllUsers(Pageable pageable) {
    log.debug("Obteniendo todos los usuarios con paginación");
    return userRepository.findAllActiveUsers(pageable);
  }

  /**
   * PATRÓN STRATEGY: Obtener usuarios activos
   */
  @Override
  @Transactional(readOnly = true)
  public List<User> getActiveUsers() {
    log.debug("Obteniendo usuarios activos");
    return userRepository.findAllActiveUsers();
  }

  /**
   * PATRÓN STRATEGY: Buscar usuarios por criterios
   */
  @Override
  @Transactional(readOnly = true)
  public Page<User> searchUsers(String searchTerm, Pageable pageable) {
    log.debug("Buscando usuarios con término: {}", searchTerm);
    return userRepository.searchUsers(searchTerm, pageable);
  }

  /**
   * PATRÓN STRATEGY: Filtrar usuarios por rol
   */
  @Override
  @Transactional(readOnly = true)
  public List<User> getUsersByRole(String roleName) {
    log.debug("Obteniendo usuarios por rol: {}", roleName);

    Role role = roleRepository.findByName(Role.RoleName.fromString(roleName))
        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));

    return userRepository.findByRoleAndDeletedAtIsNull(role);
  }

  /**
   * PATRÓN STRATEGY: Obtener usuarios por estado de verificación
   */
  @Override
  @Transactional(readOnly = true)
  public List<User> getUsersByVerificationStatus(boolean verified) {
    log.debug("Obteniendo usuarios por estado de verificación: {}", verified);
    return userRepository.findByVerifiedAndDeletedAtIsNull(verified);
  }

  /**
   * PATRÓN STRATEGY: Obtener usuarios bloqueados
   */
  @Override
  @Transactional(readOnly = true)
  public List<User> getLockedUsers() {
    log.debug("Obteniendo usuarios bloqueados");
    return userRepository.findByLockedAndDeletedAtIsNull(true);
  }

  /**
   * PATRÓN COMMAND: Bloquear usuario
   */
  @Override
  public void lockUser(Long userId, String reason, Long adminId) {
    log.debug("Bloqueando usuario ID: {} por admin ID: {}", userId, adminId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    user.setLocked(true);
    userRepository.save(user);

    // Actualizar en MongoDB
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();
        doc.setStatus("LOCKED");
        doc.addMetadata("lockReason", reason);
        doc.addMetadata("lockedAt", LocalDateTime.now());
        userDocumentRepository.save(doc);
      }
    }

    // PATRÓN OBSERVER: Registrar bloqueo
    User admin = userRepository.findById(adminId).orElse(null);
    String adminEmail = admin != null ? admin.getEmail() : "Sistema";
    logService.logUserLocked(user.getEmail(), reason, adminEmail);

    log.warn("Usuario bloqueado: {} por {}, razón: {}", user.getEmail(), adminEmail, reason);
  }

  /**
   * PATRÓN COMMAND: Desbloquear usuario
   */
  @Override
  public void unlockUser(Long userId, Long adminId) {
    log.debug("Desbloqueando usuario ID: {} por admin ID: {}", userId, adminId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    user.setLocked(false);
    userRepository.save(user);

    // Actualizar en MongoDB
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();
        doc.setStatus("ACTIVE");
        doc.getMetadata().remove("lockReason");
        doc.getMetadata().remove("lockedAt");
        userDocumentRepository.save(doc);
      }
    }

    // PATRÓN OBSERVER: Registrar desbloqueo
    User admin = userRepository.findById(adminId).orElse(null);
    String adminEmail = admin != null ? admin.getEmail() : "Sistema";
    logService.logUserUnlocked(user.getEmail(), adminEmail);

    log.info("Usuario desbloqueado: {} por {}", user.getEmail(), adminEmail);
  }

  /**
   * PATRÓN COMMAND: Cambiar rol de usuario
   */
  @Override
  public void changeUserRole(Long userId, String newRole, Long adminId) {
    log.debug("Cambiando rol de usuario ID: {} a rol: {} por admin ID: {}", userId, newRole, adminId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    Role role = roleRepository.findByName(Role.RoleName.fromString(newRole))
        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + newRole));

    String previousRole = user.getRole().getName().name();
    user.setRole(role);
    userRepository.save(user);

    // Actualizar en MongoDB
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();
        doc.setRoles(Arrays.asList(newRole));
        doc.addMetadata("previousRole", previousRole);
        doc.addMetadata("roleChangedAt", LocalDateTime.now());
        userDocumentRepository.save(doc);
      }
    }

    // PATRÓN OBSERVER: Registrar cambio de rol
    User admin = userRepository.findById(adminId).orElse(null);
    String adminEmail = admin != null ? admin.getEmail() : "Sistema";
    logService.logUserRoleChanged(user.getEmail(), previousRole, newRole, adminEmail);

    log.info("Rol cambiado para usuario: {} de {} a {} por {}",
        user.getEmail(), previousRole, newRole, adminEmail);
  }

  /**
   * PATRÓN COMMAND: Verificar usuario manualmente
   */
  @Override
  public void verifyUserManually(Long userId, Long adminId) {
    log.debug("Verificando usuario ID: {} manualmente por admin ID: {}", userId, adminId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    user.setVerified(true);
    userRepository.save(user);

    // Actualizar en MongoDB
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();
        doc.setIsVerified(true);
        doc.setStatus("ACTIVE");
        doc.addMetadata("manuallyVerifiedAt", LocalDateTime.now());
        userDocumentRepository.save(doc);
      }
    }

    // PATRÓN OBSERVER: Registrar verificación manual
    User admin = userRepository.findById(adminId).orElse(null);
    String adminEmail = admin != null ? admin.getEmail() : "Sistema";
    logService.logManualVerification(user.getEmail(), adminEmail);

    log.info("Usuario verificado manualmente: {} por {}", user.getEmail(), adminEmail);
  }

  // ===== OPERACIONES DE PREFERENCIAS =====

  /**
   * PATRÓN COMMAND: Actualizar preferencias de notificación
   */
  @Override
  public void updateNotificationPreferences(Long userId, Map<String, Boolean> preferences) {
    log.debug("Actualizando preferencias de notificación para usuario ID: {}", userId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // Actualizar en MongoDB donde se almacenan las preferencias detalladas
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();

        // PATRÓN ADAPTER: Adaptar preferencias
        UserDocument.NotificationPreferences.NotificationPreferencesBuilder prefBuilder = UserDocument.NotificationPreferences
            .builder()
            .emailAlerts(preferences.getOrDefault("emailAlerts", true))
            .inAppNotifications(preferences.getOrDefault("inAppNotifications", true))
            .marketingEmails(preferences.getOrDefault("marketingEmails", false))
            .orderUpdates(preferences.getOrDefault("orderUpdates", true));

        doc.setNotificationPreferences(prefBuilder.build());
        userDocumentRepository.save(doc);

        log.info("Preferencias de notificación actualizadas para usuario: {}", user.getEmail());
      }
    }
  }

  /**
   * PATRÓN COMMAND: Obtener preferencias de notificación
   */
  @Override
  @Transactional(readOnly = true)
  public Map<String, Boolean> getNotificationPreferences(Long userId) {
    log.debug("Obteniendo preferencias de notificación para usuario ID: {}", userId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    Map<String, Boolean> preferences = new HashMap<>();

    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent() && userDoc.get().getNotificationPreferences() != null) {
        UserDocument.NotificationPreferences prefs = userDoc.get().getNotificationPreferences();
        preferences.put("emailAlerts", prefs.getEmailAlerts());
        preferences.put("inAppNotifications", prefs.getInAppNotifications());
        preferences.put("marketingEmails", prefs.getMarketingEmails());
        preferences.put("orderUpdates", prefs.getOrderUpdates());
      }
    }

    // Valores por defecto si no hay preferencias guardadas
    if (preferences.isEmpty()) {
      preferences.put("emailAlerts", true);
      preferences.put("inAppNotifications", true);
      preferences.put("marketingEmails", false);
      preferences.put("orderUpdates", true);
    }

    return preferences;
  }

  /**
   * PATRÓN COMMAND: Actualizar configuración de privacidad
   */
  @Override
  public void updatePrivacySettings(Long userId, Map<String, Object> privacySettings) {
    log.debug("Actualizando configuración de privacidad para usuario ID: {}", userId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // Actualizar en MongoDB
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();

        // Agregar configuraciones de privacidad a metadata
        if (doc.getMetadata() == null) {
          doc.setMetadata(new HashMap<>());
        }
        doc.getMetadata().put("privacySettings", privacySettings);
        doc.getMetadata().put("privacyUpdatedAt", LocalDateTime.now());

        userDocumentRepository.save(doc);
        log.info("Configuración de privacidad actualizada para usuario: {}", user.getEmail());
      }
    }
  }

  // ===== OPERACIONES DE VALIDACIÓN =====

  /**
   * PATRÓN STRATEGY: Validar email único
   */
  @Override
  @Transactional(readOnly = true)
  public boolean isEmailUnique(String email, Long excludeUserId) {
    log.debug("Validando unicidad de email: {} excluyendo ID: {}", email, excludeUserId);

    Optional<User> existingUser = userRepository.findByEmailAndDeletedAtIsNull(email);
    return existingUser.isEmpty() || existingUser.get().getId().equals(excludeUserId);
  }

  /**
   * PATRÓN STRATEGY: Validar teléfono único
   */
  @Override
  @Transactional(readOnly = true)
  public boolean isPhoneUnique(String phone, Long excludeUserId) {
    log.debug("Validando unicidad de teléfono: {} excluyendo ID: {}", phone, excludeUserId);

    if (phone == null || phone.trim().isEmpty()) {
      return true; // Teléfono es opcional
    }

    Optional<User> existingUser = userRepository.findByPhoneAndDeletedAtIsNull(phone);
    return existingUser.isEmpty() || existingUser.get().getId().equals(excludeUserId);
  }

  /**
   * PATRÓN STRATEGY: Validar formato de email
   */
  @Override
  public boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }

    String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    return Pattern.matches(emailRegex, email);
  }

  /**
   * PATRÓN STRATEGY: Validar formato de teléfono
   */
  @Override
  public boolean isValidPhone(String phone) {
    if (phone == null || phone.trim().isEmpty()) {
      return true; // Teléfono es opcional
    }

    String phoneRegex = "^\\+?[0-9]{8,15}$";
    return Pattern.matches(phoneRegex, phone);
  }

  // ===== OPERACIONES DE ESTADÍSTICAS =====

  /**
   * PATRÓN COMMAND: Obtener estadísticas de usuario
   */
  private UserProfileResponse.UserStatistics buildUserStatistics(Long userId) {
    log.debug("Obteniendo estadísticas para usuario ID: {}", userId);

    // Por ahora, estadísticas básicas. Se pueden expandir con datos reales
    return UserProfileResponse.UserStatistics.builder()
        .totalOrders(0L)
        .totalSpent(0.0)
        .loginCount(0L)
        .productViews(0)
        .cartItems(0)
        .averageOrderValue(0.0)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getUserStatistics(Long userId) {
    Map<String, Object> stats = new HashMap<>();
    UserProfileResponse.UserStatistics userStats = buildUserStatistics(userId);

    stats.put("totalOrders", userStats.getTotalOrders());
    stats.put("totalSpent", userStats.getTotalSpent());
    stats.put("loginCount", userStats.getLoginCount());
    stats.put("productViews", userStats.getProductViews());
    stats.put("cartItems", userStats.getCartItems());
    stats.put("averageOrderValue", userStats.getAverageOrderValue());

    return stats;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getGeneralUserStatistics() {
    log.debug("Obteniendo estadísticas generales de usuarios");

    Map<String, Object> stats = new HashMap<>();

    // Estadísticas básicas
    stats.put("totalUsers", userRepository.count());
    stats.put("activeUsers", userRepository.countActiveUsersByRole(null)); // Ajustar según necesidad
    stats.put("verifiedUsers", userRepository.findByVerifiedAndDeletedAtIsNull(true).size());
    stats.put("lockedUsers", userRepository.findByLockedAndDeletedAtIsNull(true).size());
    stats.put("usersToday", userRepository.countUsersRegisteredToday());

    // Estadísticas por rol
    Map<String, Long> roleStats = new HashMap<>();
    for (Role.RoleName roleName : Role.RoleName.values()) {
      Optional<Role> role = roleRepository.findByName(roleName);
      if (role.isPresent()) {
        long count = userRepository.countActiveUsersByRole(role.get());
        roleStats.put(roleName.name(), count);
      }
    }
    stats.put("usersByRole", roleStats);

    return stats;
  }

  // ===== MÉTODOS AUXILIARES PRIVADOS =====

  /**
   * PATRÓN STRATEGY: Validar campos únicos
   */
  private void validateUniqueFields(UpdateProfileRequest request, Long userId) {
    if (!isPhoneUnique(request.getPhone(), userId)) {
      throw new RuntimeException("El teléfono ya está en uso por otro usuario");
    }

    if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
      throw new RuntimeException("El nombre ingresado  debe ser un valor válido");
    }
  }

  /**
   * PATRÓN ADAPTER: Actualizar documento de usuario desde request
   */
  private void updateUserDocumentFromRequest(String mongoUserId, UpdateProfileRequest request) {
    Optional<UserDocument> userDocOpt = userDocumentRepository.findById(mongoUserId);
    if (userDocOpt.isPresent()) {
      UserDocument doc = userDocOpt.get();

      // Actualizar datos básicos
      doc.setFirstName(request.getFirstName());
      doc.setLastName(request.getLastName());
      doc.setPhoneNumber(request.getPhone());

      // Actualizar username si se proporciona
      if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
        doc.setUsername(request.getUsername());
      }

      // Actualizar dirección detallada si se proporciona
      if (request.getAddressDetails() != null) {
        UpdateProfileRequest.Address addr = request.getAddressDetails();
        UserDocument.Address mongoAddr = UserDocument.Address.builder()
            .street(addr.getStreet())
            .city(addr.getCity())
            .state(addr.getState())
            .zipCode(addr.getZipCode())
            .country(addr.getCountry())
            .build();
        doc.setAddress(mongoAddr);
      }

      // Actualizar preferencias de notificación si se proporcionan
      if (request.getNotificationPreferences() != null) {
        UserDocument.NotificationPreferences prefs = UserDocument.NotificationPreferences.builder()
            .emailAlerts(request.getNotificationPreferences().getOrDefault("emailAlerts", true))
            .inAppNotifications(request.getNotificationPreferences().getOrDefault("inAppNotifications", true))
            .marketingEmails(request.getNotificationPreferences().getOrDefault("marketingEmails", false))
            .orderUpdates(request.getNotificationPreferences().getOrDefault("orderUpdates", true))
            .build();
        doc.setNotificationPreferences(prefs);
      }

      // Actualizar configuraciones de privacidad si se proporcionan
      if (request.getPrivacySettings() != null) {
        if (doc.getMetadata() == null) {
          doc.setMetadata(new HashMap<>());
        }
        doc.getMetadata().put("privacySettings", request.getPrivacySettings());
      }

      userDocumentRepository.save(doc);
    }
  }

  // ===== IMPLEMENTACIÓN DE MÉTODOS RESTANTES =====

  @Override
  @Transactional(readOnly = true)
  public long countUsersByStatus(String status) {
    // Implementar conteo por status en MongoDB
    return userDocumentRepository.countByStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public long countUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return userRepository.findByCreatedAtBetween(startDate, endDate).size();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getUserRecentActivity(Long userId, int limit) {
    User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
    if (user == null)
      return new ArrayList<>();

    // Obtener actividad reciente del usuario desde logs
    List<Map<String, Object>> activities = logService.getUserActivity(user.getEmail(),
        LocalDateTime.now().minusDays(30), LocalDateTime.now())
        .stream()
        .limit(limit)
        .map(log -> {
          Map<String, Object> activity = new HashMap<>();
          activity.put("timestamp", log.getTimestamp());
          activity.put("eventType", log.getEventType());
          activity.put("description", log.getDescription());
          activity.put("ipAddress", log.getIpAddress());
          return activity;
        })
        .collect(Collectors.toList());

    return activities;
  }

  @Override
  @Async
  public void synchronizeUser(Long userId) {
    log.debug("Sincronizando usuario ID: {}", userId);

    Optional<User> userOpt = userRepository.findByIdAndDeletedAtIsNull(userId);
    if (userOpt.isEmpty()) {
      log.warn("Usuario no encontrado para sincronización: {}", userId);
      return;
    }

    User user = userOpt.get();

    // Buscar o crear documento en MongoDB
    Optional<UserDocument> userDocOpt = user.getMongoUserId() != null
        ? userDocumentRepository.findById(user.getMongoUserId())
        : userDocumentRepository.findByPostgresUserId(userId);

    UserDocument userDoc;
    if (userDocOpt.isPresent()) {
      // Actualizar documento existente
      userDoc = userDocOpt.get();
      updateUserDocumentFromUser(userDoc, user);
    } else {
      // Crear nuevo documento
      userDoc = createUserDocumentFromUser(user);
    }

    UserDocument savedDoc = userDocumentRepository.save(userDoc);

    // Actualizar referencia en PostgreSQL si es necesario
    if (user.getMongoUserId() == null) {
      user.setMongoUserId(savedDoc.getId());
      userRepository.save(user);
    }

    log.info("Usuario sincronizado exitosamente: {}", user.getEmail());
  }

  @Override
  @Async
  public void synchronizeAllUsers() {
    log.info("Iniciando sincronización masiva de usuarios");

    List<User> users = userRepository.findUsersForMongoSync();
    int syncCount = 0;

    for (User user : users) {
      try {
        synchronizeUser(user.getId());
        syncCount++;
      } catch (Exception e) {
        log.error("Error sincronizando usuario {}: {}", user.getEmail(), e.getMessage());
      }
    }

    log.info("Sincronización masiva completada: {} usuarios sincronizados", syncCount);
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> validateDataIntegrity() {
    log.debug("Validando integridad de datos entre PostgreSQL y MongoDB");

    List<String> issues = new ArrayList<>();

    // Validar usuarios en PostgreSQL sin referencia MongoDB
    List<User> usersWithoutMongo = userRepository.findUsersForMongoSync();
    if (!usersWithoutMongo.isEmpty()) {
      issues.add(String.format("Usuarios en PostgreSQL sin documento MongoDB: %d", usersWithoutMongo.size()));
    }

    // Validar documentos en MongoDB sin referencia PostgreSQL
    List<UserDocument> docsWithoutPostgres = userDocumentRepository.findUsersWithoutPostgresReference();
    if (!docsWithoutPostgres.isEmpty()) {
      issues.add(String.format("Documentos en MongoDB sin referencia PostgreSQL: %d", docsWithoutPostgres.size()));
    }

    // Validar consistencia de emails
    List<User> allUsers = userRepository.findAll();
    for (User user : allUsers) {
      if (user.getMongoUserId() != null) {
        Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
        if (userDoc.isPresent() && !user.getEmail().equals(userDoc.get().getEmail())) {
          issues.add(String.format("Email inconsistente para usuario ID %d: PG='%s', Mongo='%s'",
              user.getId(), user.getEmail(), userDoc.get().getEmail()));
        }
      }
    }

    log.info("Validación de integridad completada. Issues encontradas: {}", issues.size());
    return issues;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> exportUserData(Long userId) {
    log.debug("Exportando datos para usuario ID: {}", userId);

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    Map<String, Object> userData = new HashMap<>();

    // Datos de PostgreSQL
    userData.put("id", user.getId());
    userData.put("firstName", user.getFirstName());
    userData.put("lastName", user.getLastName());
    userData.put("email", user.getEmail());
    userData.put("phone", user.getPhone());
    userData.put("birthDate", user.getBirthDate());
    userData.put("address", user.getAddress());
    userData.put("verified", user.getVerified());
    userData.put("createdAt", user.getCreatedAt());
    userData.put("lastLogin", user.getLastLogin());
    userData.put("role", user.getRole().getName().name());

    // Datos de MongoDB si existen
    if (user.getMongoUserId() != null) {
      Optional<UserDocument> userDoc = userDocumentRepository.findById(user.getMongoUserId());
      if (userDoc.isPresent()) {
        UserDocument doc = userDoc.get();
        userData.put("username", doc.getUsername());
        userData.put("addressDetails", doc.getAddress());
        userData.put("notificationPreferences", doc.getNotificationPreferences());
        userData.put("registrationDate", doc.getRegistrationDate());
        userData.put("metadata", doc.getMetadata());
      }
    }

    // Actividad reciente
    userData.put("recentActivity", getUserRecentActivity(userId, 50));
    userData.put("statistics", getUserStatistics(userId));

    return userData;
  }

  @Override
  public String exportUsersList(List<Long> userIds, String format) {
    log.debug("Exportando lista de usuarios en formato: {}", format);

    // Por ahora, solo generar nombre de archivo
    String filename = "users_export_" + System.currentTimeMillis() + "." + format.toLowerCase();

    // Aquí implementarías la lógica real de exportación según el formato
    // CSV, Excel, JSON, etc.

    return filename;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> generateUsersReport(LocalDateTime startDate, LocalDateTime endDate) {
    log.debug("Generando reporte de usuarios desde {} hasta {}", startDate, endDate);

    Map<String, Object> report = new HashMap<>();

    // Usuarios registrados en el período
    List<User> usersInPeriod = userRepository.findByCreatedAtBetween(startDate, endDate);
    report.put("usersRegisteredInPeriod", usersInPeriod.size());

    // Usuarios activos en el período
    List<User> activeUsers = userRepository.findByLastLoginBetween(startDate, endDate);
    report.put("activeUsersInPeriod", activeUsers.size());

    // Estadísticas por rol
    Map<String, Long> roleStats = new HashMap<>();
    for (Role.RoleName roleName : Role.RoleName.values()) {
      Optional<Role> role = roleRepository.findByName(roleName);
      if (role.isPresent()) {
        long count = usersInPeriod.stream()
            .filter(u -> u.getRole().equals(role.get()))
            .count();
        roleStats.put(roleName.name(), count);
      }
    }
    report.put("roleDistribution", roleStats);

    // Estadísticas generales
    report.put("totalUsers", userRepository.count());
    report.put("verifiedUsers", getUsersByVerificationStatus(true).size());
    report.put("lockedUsers", getLockedUsers().size());

    report.put("periodStart", startDate);
    report.put("periodEnd", endDate);
    report.put("generatedAt", LocalDateTime.now());

    return report;
  }

  // ===== MÉTODOS NO IMPLEMENTADOS (PLACEHOLDERS) =====

  @Override
  public boolean canUserPerformAction(Long userId, String action) {
    // Implementar lógica de permisos basada en roles
    return true;
  }

  @Override
  public boolean hasPermission(Long userId, String permission) {
    // Implementar sistema de permisos granular
    return true;
  }

  @Override
  public List<Map<String, Object>> getActiveUserSessions(Long userId) {
    // Implementar obtención de sesiones activas
    return new ArrayList<>();
  }

  @Override
  public void terminateAllUserSessions(Long userId, String reason) {
    // Implementar terminación de sesiones
    log.info("Terminando todas las sesiones para usuario ID: {}, razón: {}", userId, reason);
  }

  @Override
  public void terminateUserSession(Long userId, String sessionId) {
    // Implementar terminación de sesión específica
    log.info("Terminando sesión {} para usuario ID: {}", sessionId, userId);
  }

  @Override
  public Page<User> advancedUserSearch(Map<String, Object> filters, Pageable pageable) {
    // Por ahora, búsqueda básica
    String searchTerm = (String) filters.getOrDefault("searchTerm", "");
    return searchUsers(searchTerm, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> getUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return userRepository.findByCreatedAtBetween(startDate, endDate);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> getUsersByLocation(String city, String state) {
    // Implementar búsqueda por ubicación usando MongoDB
    List<UserDocument> docs = userDocumentRepository.findByAddressCity(city);
    return docs.stream()
        .filter(doc -> doc.getPostgresUserId() != null)
        .map(doc -> userRepository.findById(doc.getPostgresUserId()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> getInactiveUsers(int daysSinceLastLogin) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceLastLogin);
    return userRepository.findByLastLoginBetween(LocalDateTime.of(2000, 1, 1, 0, 0), cutoffDate);
  }

  @Override
  public void sendNotificationToUser(Long userId, String type, String message, Map<String, Object> data) {
    // Implementar sistema de notificaciones
    log.debug("Enviando notificación tipo {} a usuario ID: {}", type, userId);
  }

  @Override
  public void sendBulkNotification(List<Long> userIds, String type, String message) {
    // Implementar notificaciones masivas
    log.debug("Enviando notificación masiva tipo {} a {} usuarios", type, userIds.size());
  }

  @Override
  public List<Map<String, Object>> getUserNotifications(Long userId, boolean unreadOnly) {
    // Implementar obtención de notificaciones
    return new ArrayList<>();
  }

  @Override
  public void markNotificationsAsRead(Long userId, List<String> notificationIds) {
    // Implementar marcado de notificaciones como leídas
    log.debug("Marcando {} notificaciones como leídas para usuario ID: {}", notificationIds.size(), userId);
  }

  @Override
  public int cleanupInactiveUsers(int daysInactive) {
    // Implementar limpieza de usuarios inactivos
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
    List<UserDocument> inactiveUsers = userDocumentRepository.findInactiveUsers(cutoffDate);
    log.info("Encontrados {} usuarios inactivos para limpieza", inactiveUsers.size());
    return inactiveUsers.size();
  }

  @Override
  public void cleanupUserExpiredTokens(Long userId) {
    // Implementar limpieza de tokens expirados
    log.debug("Limpiando tokens expirados para usuario ID: {}", userId);
  }

  @Override
  public int cleanupExpiredSessions() {
    // Implementar limpieza de sesiones expiradas
    log.debug("Limpiando sesiones expiradas");
    return 0;
  }

  // ===== MÉTODOS AUXILIARES ADICIONALES =====

  /**
   * PATRÓN ADAPTER: Crear UserDocument desde User
   */
  private UserDocument createUserDocumentFromUser(User user) {
    return UserDocument.builder()
        .username(user.getEmail()) // Usar email como username por defecto
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .phoneNumber(user.getPhone())
        .postgresUserId(user.getId())
        .status(user.isEnabled() ? "ACTIVE" : "INACTIVE")
        .isVerified(user.isVerified())
        .registrationDate(user.getCreatedAt())
        .lastLogin(user.getLastLogin())
        .roles(Arrays.asList(user.getRole().getName().name()))
        .notificationPreferences(UserDocument.NotificationPreferences.builder().build())
        .build();
  }

  /**
   * PATRÓN ADAPTER: Actualizar UserDocument desde User
   */
  private void updateUserDocumentFromUser(UserDocument doc, User user) {
    doc.setEmail(user.getEmail());
    doc.setFirstName(user.getFirstName());
    doc.setLastName(user.getLastName());
    doc.setPhoneNumber(user.getPhone());
    doc.setPostgresUserId(user.getId());
    doc.setStatus(user.isEnabled() ? "ACTIVE" : "INACTIVE");
    doc.setIsVerified(user.isVerified());
    doc.setLastLogin(user.getLastLogin());
    doc.setRoles(Arrays.asList(user.getRole().getName().name()));
  }

  /**
   * Métodos adicionales de LogService que se necesitan
   */
  private void addMissingLogMethods() {
    // Estos métodos se deberían agregar a LogService si no existen
    // logService.logUserUpdate(String email, String ipAddress)
    // logService.logUserDeleted(String email, String reason, String adminEmail)
    // logService.logUserRestored(String email, String adminEmail)
    // logService.logUserRoleChanged(String email, String oldRole, String newRole,
    // String adminEmail)
    // logService.logManualVerification(String email, String adminEmail)
  }
}