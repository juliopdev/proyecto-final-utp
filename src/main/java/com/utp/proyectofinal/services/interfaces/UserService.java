package com.utp.proyectofinal.services.interfaces;

import com.utp.proyectofinal.models.documents.UserDocument;
import com.utp.proyectofinal.models.dto.UpdateProfileRequest;
import com.utp.proyectofinal.models.dto.UserProfileResponse;
import com.utp.proyectofinal.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface para el servicio de gestión de usuarios
 * 
 * PATRONES IMPLEMENTADOS:
 * - FACADE: Proporciona una fachada simplificada para operaciones de usuario
 * - COMMAND: Cada método representa un comando de gestión de usuario
 * - STRATEGY: Diferentes estrategias de búsqueda y filtrado
 * - REPOSITORY: Abstrae el acceso a datos de usuario
 * 
 * @author Julio Pariona
 */
public interface UserService {

    // ===== OPERACIONES BÁSICAS DE USUARIO =====

    /**
     * PATRÓN COMMAND: Obtener usuario por ID
     */
    Optional<User> findById(Long id);

    /**
     * PATRÓN COMMAND: Obtener usuario por email
     */
    Optional<User> findByEmail(String email);

    /**
     * PATRÓN COMMAND: Obtener documento de usuario por email
     */
    Optional<UserDocument> findUserDocumentByEmail(String email);

    /**
     * PATRÓN COMMAND: Obtener perfil completo de usuario
     */
    UserProfileResponse getUserProfile(Long userId);

    /**
     * PATRÓN COMMAND: Actualizar perfil de usuario
     */
    User updateUserProfile(Long userId, UpdateProfileRequest updateRequest, String ipAddress);

    /**
     * PATRÓN COMMAND: Eliminar usuario (soft delete)
     */
    void deleteUser(Long userId, String reason, Long adminId);

    /**
     * PATRÓN COMMAND: Restaurar usuario eliminado
     */
    void restoreUser(Long userId, Long adminId);

    // ===== OPERACIONES DE ADMINISTRACIÓN =====

    /**
     * PATRÓN STRATEGY: Obtener todos los usuarios con paginación
     */
    Page<User> getAllUsers(Pageable pageable);

    /**
     * PATRÓN STRATEGY: Obtener usuarios activos
     */
    List<User> getActiveUsers();

    /**
     * PATRÓN STRATEGY: Buscar usuarios por criterios
     */
    Page<User> searchUsers(String searchTerm, Pageable pageable);

    /**
     * PATRÓN STRATEGY: Filtrar usuarios por rol
     */
    List<User> getUsersByRole(String roleName);

    /**
     * PATRÓN STRATEGY: Obtener usuarios verificados/no verificados
     */
    List<User> getUsersByVerificationStatus(boolean verified);

    /**
     * PATRÓN STRATEGY: Obtener usuarios bloqueados
     */
    List<User> getLockedUsers();

    /**
     * PATRÓN COMMAND: Bloquear usuario
     */
    void lockUser(Long userId, String reason, Long adminId);

    /**
     * PATRÓN COMMAND: Desbloquear usuario
     */
    void unlockUser(Long userId, Long adminId);

    /**
     * PATRÓN COMMAND: Cambiar rol de usuario
     */
    void changeUserRole(Long userId, String newRole, Long adminId);

    /**
     * PATRÓN COMMAND: Verificar usuario manualmente
     */
    void verifyUserManually(Long userId, Long adminId);

    // ===== OPERACIONES DE PREFERENCIAS =====

    /**
     * PATRÓN COMMAND: Actualizar preferencias de notificación
     */
    void updateNotificationPreferences(Long userId, Map<String, Boolean> preferences);

    /**
     * PATRÓN COMMAND: Obtener preferencias de notificación
     */
    Map<String, Boolean> getNotificationPreferences(Long userId);

    /**
     * PATRÓN COMMAND: Actualizar configuración de privacidad
     */
    void updatePrivacySettings(Long userId, Map<String, Object> privacySettings);

    // ===== OPERACIONES DE SEGURIDAD =====

    /**
     * PATRÓN STRATEGY: Validar si el usuario puede realizar una acción
     */
    boolean canUserPerformAction(Long userId, String action);

    /**
     * PATRÓN STRATEGY: Verificar si el usuario tiene permisos
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * PATRÓN COMMAND: Obtener sesiones activas del usuario
     */
    List<Map<String, Object>> getActiveUserSessions(Long userId);

    /**
     * PATRÓN COMMAND: Cerrar todas las sesiones del usuario
     */
    void terminateAllUserSessions(Long userId, String reason);

    /**
     * PATRÓN COMMAND: Cerrar sesión específica
     */
    void terminateUserSession(Long userId, String sessionId);

    // ===== OPERACIONES DE ESTADÍSTICAS =====

    /**
     * PATRÓN COMMAND: Obtener estadísticas de usuario
     */
    Map<String, Object> getUserStatistics(Long userId);

    /**
     * PATRÓN COMMAND: Obtener estadísticas generales de usuarios
     */
    Map<String, Object> getGeneralUserStatistics();

    /**
     * PATRÓN STRATEGY: Contar usuarios por estado
     */
    long countUsersByStatus(String status);

    /**
     * PATRÓN STRATEGY: Contar usuarios registrados en período
     */
    long countUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * PATRÓN COMMAND: Obtener actividad reciente del usuario
     */
    List<Map<String, Object>> getUserRecentActivity(Long userId, int limit);

    // ===== OPERACIONES DE SINCRONIZACIÓN =====

    /**
     * PATRÓN COMMAND: Sincronizar usuario entre PostgreSQL y MongoDB
     */
    void synchronizeUser(Long userId);

    /**
     * PATRÓN COMMAND: Sincronizar todos los usuarios
     */
    void synchronizeAllUsers();

    /**
     * PATRÓN STRATEGY: Verificar integridad de datos entre bases
     */
    List<String> validateDataIntegrity();

    // ===== OPERACIONES DE EXPORTACIÓN =====

    /**
     * PATRÓN COMMAND: Exportar datos del usuario
     */
    Map<String, Object> exportUserData(Long userId);

    /**
     * PATRÓN COMMAND: Exportar lista de usuarios
     */
    String exportUsersList(List<Long> userIds, String format);

    /**
     * PATRÓN COMMAND: Generar reporte de usuarios
     */
    Map<String, Object> generateUsersReport(LocalDateTime startDate, LocalDateTime endDate);

    // ===== OPERACIONES DE VALIDACIÓN =====

    /**
     * PATRÓN STRATEGY: Validar que el email sea único
     */
    boolean isEmailUnique(String email, Long excludeUserId);

    /**
     * PATRÓN STRATEGY: Validar que el teléfono sea único
     */
    boolean isPhoneUnique(String phone, Long excludeUserId);

    /**
     * PATRÓN STRATEGY: Validar formato de email
     */
    boolean isValidEmail(String email);

    /**
     * PATRÓN STRATEGY: Validar formato de teléfono
     */
    boolean isValidPhone(String phone);

    // ===== OPERACIONES DE BÚSQUEDA AVANZADA =====

    /**
     * PATRÓN STRATEGY: Búsqueda avanzada con múltiples filtros
     */
    Page<User> advancedUserSearch(Map<String, Object> filters, Pageable pageable);

    /**
     * PATRÓN STRATEGY: Obtener usuarios por rango de fechas
     */
    List<User> getUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * PATRÓN STRATEGY: Obtener usuarios por ubicación
     */
    List<User> getUsersByLocation(String city, String state);

    /**
     * PATRÓN STRATEGY: Obtener usuarios inactivos
     */
    List<User> getInactiveUsers(int daysSinceLastLogin);

    // ===== OPERACIONES DE NOTIFICACIÓN =====

    /**
     * PATRÓN COMMAND: Enviar notificación a usuario
     */
    void sendNotificationToUser(Long userId, String type, String message, Map<String, Object> data);

    /**
     * PATRÓN COMMAND: Enviar notificación masiva
     */
    void sendBulkNotification(List<Long> userIds, String type, String message);

    /**
     * PATRÓN COMMAND: Obtener notificaciones del usuario
     */
    List<Map<String, Object>> getUserNotifications(Long userId, boolean unreadOnly);

    /**
     * PATRÓN COMMAND: Marcar notificaciones como leídas
     */
    void markNotificationsAsRead(Long userId, List<String> notificationIds);

    // ===== OPERACIONES DE LIMPIEZA =====

    /**
     * PATRÓN COMMAND: Limpiar usuarios inactivos
     */
    int cleanupInactiveUsers(int daysInactive);

    /**
     * PATRÓN COMMAND: Limpiar tokens expirados del usuario
     */
    void cleanupUserExpiredTokens(Long userId);

    /**
     * PATRÓN COMMAND: Limpiar sesiones expiradas
     */
    int cleanupExpiredSessions();
}