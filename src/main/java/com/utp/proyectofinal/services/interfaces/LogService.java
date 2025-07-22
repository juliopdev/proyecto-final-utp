package com.utp.proyectofinal.services.interfaces;

import com.utp.proyectofinal.models.documents.LogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface para el servicio de logging y auditoría
 * 
 * PATRONES IMPLEMENTADOS:
 * - OBSERVER: Observa y registra eventos del sistema
 * - COMMAND: Cada método de logging es un comando
 * - MEMENTO: Almacena estados históricos para auditoría
 * - STRATEGY: Diferentes estrategias de logging según el tipo de evento
 * 
 * @author Julio Pariona
 */
public interface LogService {

  // ===== MÉTODOS DE LOGGING DE EVENTOS =====

  /**
   * PATRÓN OBSERVER: Registrar login de usuario
   */
  void logUserLogin(String userEmail, String ipAddress, String platform, boolean success);

  /**
   * PATRÓN OBSERVER: Registrar logout de usuario
   */
  void logUserLogout(String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar login fallido
   */
  void logFailedLogin(String email, String ipAddress, String reason);

  /**
   * PATRÓN OBSERVER: Registrar registro de usuario
   */
  void logUserRegistration(String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar error en registro
   */
  void logRegistrationError(String email, String errorMessage, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar verificación de email
   */
  void logEmailVerification(String userEmail);

  /**
   * PATRÓN OBSERVER: Registrar solicitud de reset de contraseña
   */
  void logPasswordResetRequest(String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar reset de contraseña exitoso
   */
  void logPasswordReset(String userEmail, String method);

  /**
   * PATRÓN OBSERVER: Registrar intento de cambio de contraseña
   */
  void logPasswordChangeAttempt(String userEmail, String ipAddress, boolean success);

  /**
   * PATRÓN OBSERVER: Registrar bloqueo de usuario
   */
  void logUserLocked(String userEmail, String reason, String adminEmail);

  /**
   * PATRÓN OBSERVER: Registrar desbloqueo de usuario
   */
  void logUserUnlocked(String userEmail, String adminEmail);

  /**
   * PATRÓN OBSERVER: Registrar creación de producto
   */
  void logProductCreated(String productName, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar actualización de producto
   */
  void logProductUpdated(String productName, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar eliminación de producto
   */
  void logProductDeleted(String productName, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar visualización de producto
   */
  void logProductViewed(String productName, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar agregado al carrito
   */
  void logCartItemAdded(String productName, int quantity, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar eliminado del carrito
   */
  void logCartItemRemoved(String productName, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar creación de pedido
   */
  void logOrderCreated(Long orderId, String userEmail, String ipAddress, Double totalAmount);

  /**
   * PATRÓN OBSERVER: Registrar actualización de pedido
   */
  void logOrderUpdated(Long orderId, String status, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar cancelación de pedido
   */
  void logOrderCancelled(Long orderId, String reason, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar inicio de pago
   */
  void logPaymentInitiated(Long orderId, String paymentMethod, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar pago exitoso
   */
  void logPaymentSuccess(Long orderId, String transactionId, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar pago fallido
   */
  void logPaymentFailed(Long orderId, String errorReason, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar acceso a API
   */
  void logApiAccess(String endpoint, String method, String userEmail, String ipAddress, int responseCode);

  /**
   * PATRÓN OBSERVER: Registrar actividad sospechosa
   */
  void logSuspiciousActivity(String activity, String userEmail, String ipAddress, Map<String, Object> details);

  /**
   * PATRÓN OBSERVER: Registrar acceso no autorizado
   */
  void logUnauthorizedAccess(String resource, String userEmail, String ipAddress);

  /**
   * PATRÓN OBSERVER: Registrar error del sistema
   */
  void logSystemError(String errorMessage, String stackTrace, String module);

  /**
   * PATRÓN OBSERVER: Registrar exportación de datos
   */
  void logDataExport(String dataType, String userEmail, String ipAddress);

  // ===== MÉTODOS DE CONSULTA Y ANÁLISIS =====

  /**
   * PATRÓN COMMAND: Obtener logs por usuario
   */
  List<LogDocument> getLogsByUser(String userEmail, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN COMMAND: Obtener logs por tipo de evento
   */
  List<LogDocument> getLogsByEventType(String eventType, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN COMMAND: Obtener logs de seguridad
   */
  Page<LogDocument> getSecurityLogs(Pageable pageable);

  /**
   * PATRÓN COMMAND: Obtener logs de error
   */
  Page<LogDocument> getErrorLogs(Pageable pageable);

  /**
   * PATRÓN COMMAND: Obtener logs de autenticación
   */
  List<LogDocument> getAuthenticationLogs(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN COMMAND: Obtener intentos de login fallidos por usuario
   */
  List<LogDocument> getFailedLoginAttempts(String userEmail, LocalDateTime since);

  /**
   * PATRÓN COMMAND: Obtener intentos de login fallidos por IP
   */
  List<LogDocument> getFailedLoginAttemptsByIp(String ipAddress, LocalDateTime since);

  /**
   * PATRÓN COMMAND: Obtener logs recientes
   */
  List<LogDocument> getRecentLogs(int limit);

  /**
   * PATRÓN STRATEGY: Contar intentos de login fallidos
   */
  long countFailedLoginAttempts(String userEmail, LocalDateTime since);

  /**
   * PATRÓN STRATEGY: Contar intentos de login fallidos por IP
   */
  long countFailedLoginAttemptsByIp(String ipAddress, LocalDateTime since);

  /**
   * PATRÓN COMMAND: Buscar logs por descripción
   */
  List<LogDocument> searchLogs(String searchTerm, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN COMMAND: Obtener estadísticas de logs
   */
  Map<String, Long> getLogStatistics(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN COMMAND: Obtener actividad de usuario en rango de tiempo
   */
  List<LogDocument> getUserActivity(String userEmail, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN COMMAND: Obtener logs por módulo
   */
  List<LogDocument> getLogsByModule(String module, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN STRATEGY: Verificar si hay actividad sospechosa reciente
   */
  boolean hasSuspiciousActivityRecent(String userEmail, int hoursBack);

  /**
   * PATRÓN STRATEGY: Verificar si IP tiene muchos intentos fallidos
   */
  boolean isIpSuspicious(String ipAddress, int hoursBack, int maxAttempts);

  // ===== MÉTODOS DE ADMINISTRACIÓN =====

  /**
   * PATRÓN COMMAND: Limpiar logs antiguos
   */
  void cleanupOldLogs(int daysToKeep);

  /**
   * PATRÓN COMMAND: Archivar logs antiguos
   */
  void archiveOldLogs(int daysToArchive);

  /**
   * PATRÓN COMMAND: Exportar logs a archivo
   */
  String exportLogsToFile(LocalDateTime startDate, LocalDateTime endDate, String format);

  /**
   * PATRÓN STRATEGY: Obtener métricas de rendimiento del sistema
   */
  Map<String, Object> getSystemMetrics();

  /**
   * PATRÓN COMMAND: Generar reporte de seguridad
   */
  Map<String, Object> generateSecurityReport(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN COMMAND: Generar reporte de actividad de usuarios
   */
  Map<String, Object> generateUserActivityReport(LocalDateTime startDate, LocalDateTime endDate);

  // ===== MÉTODOS DE LOGGING GENÉRICO =====

  /**
   * PATRÓN COMMAND: Log genérico con nivel personalizado
   */
  void logEvent(String eventType, String description, String userEmail, String ipAddress,
      String level, String module, Map<String, Object> details);

  /**
   * PATRÓN COMMAND: Log de información
   */
  void logInfo(String eventType, String description, String userEmail, String ipAddress);

  /**
   * PATRÓN COMMAND: Log de advertencia
   */
  void logWarning(String eventType, String description, String userEmail, String ipAddress);

  /**
   * PATRÓN COMMAND: Log de error
   */
  void logError(String eventType, String description, String userEmail, String ipAddress,
      Map<String, Object> errorDetails);

  /**
   * PATRÓN COMMAND: Log de debug
   */
  void logDebug(String eventType, String description, Map<String, Object> details);

  /**
   * PATRÓN COMMAND: Log de actualizacion del usuario
   */
  void logUserUpdate(String userEmail, String ipAddress);

  /**
   * PATRÓN COMMAND: Log de usuario eliminado
   */
  void logUserDeleted(String userEmail, String reason, String adminEmail);

  /**
   * PATRÓN COMMAND: Log de usuario restaurado
   */
  void logUserRestored(String userEmail, String adminEmail);

  /**
   * PATRÓN COMMAND: Log de rol de usuario cambiado
   */
  void logUserRoleChanged(String userEmail, String oldRole, String newRole, String adminEmail);

  /**
   * PATRÓN COMMAND: Log de verificaciones
   */
  void logManualVerification(String userEmail, String adminEmail);

  void logPageView(String page, String userEmail, String ipAddress);

  void logContactFormSubmission(String name, String email, String subject, String ipAddress);

  void logProductListing(String category, long resultCount, String userEmail, String ipAddress);

  void logQuickSearch(String query, int resultCount, String userEmail, String ipAddress);

  void logProductFilter(String filters, long resultCount, String userEmail, String ipAddress);

  void logDashboardAccess(String userEmail, String ipAddress);

  void logProfileUpdate(String userEmail, String ipAddress);

  void logNotificationPreferencesUpdate(String userEmail, String ipAddress);

  void logAdminDashboardAccess(String userEmail, String ipAddress);

  void logPageNotFound(String requestUri, String userEmail, String ipAddress);

  void logSearchQuery(String query, String category, String userEmail, String ipAddress);

}