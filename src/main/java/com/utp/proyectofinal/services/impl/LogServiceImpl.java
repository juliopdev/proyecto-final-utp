package com.utp.proyectofinal.services.impl;

import com.utp.proyectofinal.models.documents.LogDocument;
import com.utp.proyectofinal.repositories.mongodb.LogDocumentRepository;
import com.utp.proyectofinal.services.interfaces.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de logging y auditoría
 * 
 * PATRONES IMPLEMENTADOS:
 * - OBSERVER: Implementa el patrón Observer para registrar eventos del sistema
 * - COMMAND: Cada método encapsula un comando de logging
 * - STRATEGY: Diferentes estrategias de logging según el contexto
 * - FACTORY: Factory methods para crear diferentes tipos de logs
 * - SINGLETON: Service singleton que mantiene el estado de logging
 * 
 * @author Julio Pariona
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogServiceImpl implements LogService {

  private final LogDocumentRepository logRepository;

  // ===== IMPLEMENTACIÓN DE EVENTOS DE AUTENTICACIÓN =====

  /**
   * PATRÓN OBSERVER: Registra eventos de login
   * PATRÓN ASYNC: Ejecuta de forma asíncrona para no bloquear el flujo principal
   */
  @Override
  @Async
  public void logUserLogin(String userEmail, String ipAddress, String platform, boolean success) {
    String eventType = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
    String description = success ? String.format("Usuario %s inició sesión exitosamente desde %s", userEmail, platform)
        : String.format("Intento de login fallido para %s desde %s", userEmail, platform);

    Map<String, Object> details = new HashMap<>();
    details.put("platform", platform);
    details.put("success", success);
    details.put("userAgent", getCurrentUserAgent());

    LogDocument logDoc = createLogDocument(eventType, userEmail, ipAddress, description,
        success ? "INFO" : "WARN", "AUTH", details);

    saveLogAsync(logDoc);
    log.debug("Login event logged for: {} - Success: {}", userEmail, success);
  }

  @Override
  @Async
  public void logUserLogout(String userEmail, String ipAddress) {
    String description = String.format("Usuario %s cerró sesión", userEmail);

    LogDocument logDoc = createLogDocument("LOGOUT", userEmail, ipAddress, description,
        "INFO", "AUTH", null);
    saveLogAsync(logDoc);
    log.debug("Logout event logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logFailedLogin(String email, String ipAddress, String reason) {
    String description = String.format("Intento de login fallido: %s", reason);

    Map<String, Object> details = new HashMap<>();
    details.put("reason", reason);
    details.put("attemptCount", countRecentFailedAttempts(email));

    LogDocument logDoc = createLogDocument("LOGIN_FAILED", email, ipAddress, description,
        "WARN", "AUTH", details);
    saveLogAsync(logDoc);
    log.warn("Failed login attempt logged for: {} - Reason: {}", email, reason);
  }

  @Override
  @Async
  public void logUserRegistration(String userEmail, String ipAddress) {
    String description = String.format("Nuevo usuario registrado: %s", userEmail);

    LogDocument logDoc = createLogDocument("USER_CREATED", userEmail, ipAddress, description,
        "INFO", "USER", null);
    saveLogAsync(logDoc);
    log.info("User registration logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logRegistrationError(String email, String errorMessage, String ipAddress) {
    String description = String.format("Error en registro de usuario: %s", errorMessage);

    Map<String, Object> details = new HashMap<>();
    details.put("errorMessage", errorMessage);

    LogDocument logDoc = createLogDocument("USER_REGISTRATION_ERROR", email, ipAddress,
        description, "ERROR", "USER", details);
    saveLogAsync(logDoc);
    log.error("Registration error logged for: {} - Error: {}", email, errorMessage);
  }

  @Override
  @Async
  public void logEmailVerification(String userEmail) {
    String description = String.format("Email verificado para usuario: %s", userEmail);

    LogDocument logDoc = createLogDocument("EMAIL_VERIFIED", userEmail, null, description,
        "INFO", "USER", null);
    saveLogAsync(logDoc);
    log.info("Email verification logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logPasswordResetRequest(String userEmail, String ipAddress) {
    String description = String.format("Solicitud de reset de contraseña para: %s", userEmail);

    LogDocument logDoc = createLogDocument("PASSWORD_RESET_REQUEST", userEmail, ipAddress,
        description, "INFO", "AUTH", null);
    saveLogAsync(logDoc);
    log.info("Password reset request logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logPasswordReset(String userEmail, String method) {
    String description = String.format("Contraseña reseteada para %s usando método: %s", userEmail, method);

    Map<String, Object> details = new HashMap<>();
    details.put("resetMethod", method);

    LogDocument logDoc = createLogDocument("PASSWORD_CHANGE", userEmail, null, description,
        "INFO", "AUTH", details);
    saveLogAsync(logDoc);
    log.info("Password reset logged for: {} using method: {}", userEmail, method);
  }

  @Override
  @Async
  public void logPasswordChangeAttempt(String userEmail, String ipAddress, boolean success) {
    String description = success ? String.format("Contraseña cambiada exitosamente para: %s", userEmail)
        : String.format("Intento fallido de cambio de contraseña para: %s", userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("success", success);

    LogDocument logDoc = createLogDocument("PASSWORD_CHANGE", userEmail, ipAddress, description,
        success ? "INFO" : "WARN", "AUTH", details);
    saveLogAsync(logDoc);
    log.debug("Password change attempt logged for: {} - Success: {}", userEmail, success);
  }

  @Override
  @Async
  public void logUserLocked(String userEmail, String reason, String adminEmail) {
    String description = String.format("Usuario %s bloqueado por %s. Razón: %s", userEmail, adminEmail, reason);

    Map<String, Object> details = new HashMap<>();
    details.put("reason", reason);
    details.put("adminEmail", adminEmail);

    LogDocument logDoc = createLogDocument("ACCOUNT_LOCKED", userEmail, null, description,
        "WARN", "ADMIN", details);
    saveLogAsync(logDoc);
    log.warn("User lock logged: {} by {} - Reason: {}", userEmail, adminEmail, reason);
  }

  @Override
  @Async
  public void logUserUnlocked(String userEmail, String adminEmail) {
    String description = String.format("Usuario %s desbloqueado por %s", userEmail, adminEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("adminEmail", adminEmail);

    LogDocument logDoc = createLogDocument("ACCOUNT_UNLOCKED", userEmail, null, description,
        "INFO", "ADMIN", details);
    saveLogAsync(logDoc);
    log.info("User unlock logged: {} by {}", userEmail, adminEmail);
  }

  @Override
  @Async
  public void logUserUpdate(String userEmail, String ipAddress) {
    String description = String.format("Perfil actualizado para usuario: %s", userEmail);

    LogDocument logDoc = createLogDocument("USER_UPDATED", userEmail, ipAddress, description,
        "INFO", "USER", null);
    saveLogAsync(logDoc);
    log.info("User update logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logUserDeleted(String userEmail, String reason, String adminEmail) {
    String description = String.format("Usuario %s eliminado por %s. Razón: %s", userEmail, adminEmail, reason);

    Map<String, Object> details = new HashMap<>();
    details.put("reason", reason);
    details.put("adminEmail", adminEmail);
    details.put("deletionType", "SOFT_DELETE");

    LogDocument logDoc = createLogDocument("USER_DELETED", userEmail, null, description,
        "WARN", "ADMIN", details);
    saveLogAsync(logDoc);
    log.warn("User deletion logged: {} by {} - Reason: {}", userEmail, adminEmail, reason);
  }

  @Override
  @Async
  public void logUserRestored(String userEmail, String adminEmail) {
    String description = String.format("Usuario %s restaurado por %s", userEmail, adminEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("adminEmail", adminEmail);

    LogDocument logDoc = createLogDocument("USER_RESTORED", userEmail, null, description,
        "INFO", "ADMIN", details);
    saveLogAsync(logDoc);
    log.info("User restoration logged: {} by {}", userEmail, adminEmail);
  }

  @Override
  @Async
  public void logUserRoleChanged(String userEmail, String oldRole, String newRole, String adminEmail) {
    String description = String.format("Rol cambiado para %s: %s → %s por %s", userEmail, oldRole, newRole, adminEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("oldRole", oldRole);
    details.put("newRole", newRole);
    details.put("adminEmail", adminEmail);

    LogDocument logDoc = createLogDocument("USER_ROLE_CHANGED", userEmail, null, description,
        "INFO", "ADMIN", details);
    saveLogAsync(logDoc);
    log.info("User role change logged: {} {} → {} by {}", userEmail, oldRole, newRole, adminEmail);
  }

  @Override
  @Async
  public void logManualVerification(String userEmail, String adminEmail) {
    String description = String.format("Usuario %s verificado manualmente por %s", userEmail, adminEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("adminEmail", adminEmail);
    details.put("verificationType", "MANUAL");

    LogDocument logDoc = createLogDocument("USER_VERIFIED_MANUALLY", userEmail, null, description,
        "INFO", "ADMIN", details);
    saveLogAsync(logDoc);
    log.info("Manual verification logged: {} by {}", userEmail, adminEmail);
  }

  // ===== IMPLEMENTACIÓN DE EVENTOS DE PRODUCTOS =====

  @Override
  @Async
  public void logProductCreated(String productName, String userEmail, String ipAddress) {
    String description = String.format("Producto creado: %s por %s", productName, userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("productName", productName);
    details.put("action", "CREATE");

    LogDocument logDoc = createLogDocument("PRODUCT_CREATED", userEmail, ipAddress, description,
        "INFO", "PRODUCT", details);
    saveLogAsync(logDoc);
    log.info("Product creation logged: {} by {}", productName, userEmail);
  }

  @Override
  @Async
  public void logProductUpdated(String productName, String userEmail, String ipAddress) {
    String description = String.format("Producto actualizado: %s por %s", productName, userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("productName", productName);
    details.put("action", "UPDATE");

    LogDocument logDoc = createLogDocument("PRODUCT_UPDATED", userEmail, ipAddress, description,
        "INFO", "PRODUCT", details);
    saveLogAsync(logDoc);
    log.info("Product update logged: {} by {}", productName, userEmail);
  }

  @Override
  @Async
  public void logProductDeleted(String productName, String userEmail, String ipAddress) {
    String description = String.format("Producto eliminado: %s por %s", productName, userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("productName", productName);
    details.put("action", "DELETE");

    LogDocument logDoc = createLogDocument("PRODUCT_DELETED", userEmail, ipAddress, description,
        "WARN", "PRODUCT", details);
    saveLogAsync(logDoc);
    log.warn("Product deletion logged: {} by {}", productName, userEmail);
  }

  @Override
  @Async
  public void logProductViewed(String productName, String userEmail, String ipAddress) {
    String description = String.format("Producto visualizado: %s", productName);

    Map<String, Object> details = new HashMap<>();
    details.put("productName", productName);
    details.put("viewedBy", userEmail);

    LogDocument logDoc = createLogDocument("PRODUCT_VIEWED", userEmail, ipAddress, description,
        "DEBUG", "PRODUCT", details);
    saveLogAsync(logDoc);
    log.debug("Product view logged: {} by {}", productName, userEmail);
  }

  @Override
  @Async
  public void logProductListing(String category, long resultCount, String userEmail, String ipAddress) {
    String description = category != null
        ? String.format("Listado de productos por categoría '%s' - %d resultados", category, resultCount)
        : String.format("Listado general de productos - %d resultados", resultCount);

    Map<String, Object> details = new HashMap<>();
    details.put("category", category);
    details.put("resultCount", resultCount);
    details.put("listingType", "PRODUCT_CATALOG");

    LogDocument logDoc = createLogDocument("PRODUCT_LISTING", userEmail, ipAddress, description,
        "DEBUG", "PRODUCT", details);
    saveLogAsync(logDoc);
    log.debug("Product listing logged: category={}, results={}, user={}", category, resultCount, userEmail);
  }

  @Override
  @Async
  public void logProductFilter(String filters, long resultCount, String userEmail, String ipAddress) {
    String description = String.format("Filtros aplicados: %s - %d resultados", filters, resultCount);

    Map<String, Object> details = new HashMap<>();
    details.put("appliedFilters", filters);
    details.put("resultCount", resultCount);
    details.put("filterType", "ADVANCED");

    LogDocument logDoc = createLogDocument("PRODUCT_FILTER", userEmail, ipAddress, description,
        "DEBUG", "PRODUCT", details);
    saveLogAsync(logDoc);
    log.debug("Product filter logged: filters={}, results={}, user={}", filters, resultCount, userEmail);
  }

  // ===== IMPLEMENTACIÓN DE EVENTOS DE CARRITO Y PEDIDOS =====

  @Override
  @Async
  public void logCartItemAdded(String productName, int quantity, String userEmail, String ipAddress) {
    String description = String.format("Agregado al carrito: %d x %s por %s", quantity, productName, userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("productName", productName);
    details.put("quantity", quantity);
    details.put("action", "ADD_TO_CART");

    LogDocument logDoc = createLogDocument("CART_ITEM_ADDED", userEmail, ipAddress, description,
        "INFO", "CART", details);
    saveLogAsync(logDoc);
    log.debug("Cart item addition logged: {}x{} by {}", quantity, productName, userEmail);
  }

  @Override
  @Async
  public void logCartItemRemoved(String productName, String userEmail, String ipAddress) {
    String description = String.format("Removido del carrito: %s por %s", productName, userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("productName", productName);
    details.put("action", "REMOVE_FROM_CART");

    LogDocument logDoc = createLogDocument("CART_ITEM_REMOVED", userEmail, ipAddress, description,
        "INFO", "CART", details);
    saveLogAsync(logDoc);
    log.debug("Cart item removal logged: {} by {}", productName, userEmail);
  }

  @Override
  @Async
  public void logOrderCreated(Long orderId, String userEmail, String ipAddress, Double totalAmount) {
    String description = String.format("Pedido creado: #%d por %s - Total: S/ %.2f", orderId, userEmail, totalAmount);

    Map<String, Object> details = new HashMap<>();
    details.put("orderId", orderId);
    details.put("totalAmount", totalAmount);
    details.put("currency", "PEN");

    LogDocument logDoc = createLogDocument("ORDER_CREATED", userEmail, ipAddress, description,
        "INFO", "ORDER", details);

    // También agregar referencia a PostgreSQL
    logDoc.setPostgresReferenceId(orderId);

    saveLogAsync(logDoc);
    log.info("Order creation logged: #{} by {} - Amount: {}", orderId, userEmail, totalAmount);
  }

  @Override
  @Async
  public void logOrderUpdated(Long orderId, String status, String userEmail, String ipAddress) {
    String description = String.format("Pedido #%d actualizado a estado: %s", orderId, status);

    Map<String, Object> details = new HashMap<>();
    details.put("orderId", orderId);
    details.put("newStatus", status);
    details.put("updatedBy", userEmail);

    LogDocument logDoc = createLogDocument("ORDER_UPDATED", userEmail, ipAddress, description,
        "INFO", "ORDER", details);
    logDoc.setPostgresReferenceId(orderId);

    saveLogAsync(logDoc);
    log.info("Order update logged: #{} to status {} by {}", orderId, status, userEmail);
  }

  @Override
  @Async
  public void logOrderCancelled(Long orderId, String reason, String userEmail, String ipAddress) {
    String description = String.format("Pedido #%d cancelado por %s. Razón: %s", orderId, userEmail, reason);

    Map<String, Object> details = new HashMap<>();
    details.put("orderId", orderId);
    details.put("cancellationReason", reason);
    details.put("cancelledBy", userEmail);

    LogDocument logDoc = createLogDocument("ORDER_CANCELLED", userEmail, ipAddress, description,
        "WARN", "ORDER", details);
    logDoc.setPostgresReferenceId(orderId);

    saveLogAsync(logDoc);
    log.warn("Order cancellation logged: #{} by {} - Reason: {}", orderId, userEmail, reason);
  }

  // ===== IMPLEMENTACIÓN DE EVENTOS DE PAGO =====

  @Override
  @Async
  public void logPaymentInitiated(Long orderId, String paymentMethod, String userEmail, String ipAddress) {
    String description = String.format("Pago iniciado para pedido #%d usando %s", orderId, paymentMethod);

    Map<String, Object> details = new HashMap<>();
    details.put("orderId", orderId);
    details.put("paymentMethod", paymentMethod);

    LogDocument logDoc = createLogDocument("PAYMENT_INITIATED", userEmail, ipAddress, description,
        "INFO", "PAYMENT", details);
    logDoc.setPostgresReferenceId(orderId);

    saveLogAsync(logDoc);
    log.info("Payment initiation logged: #{} using {} by {}", orderId, paymentMethod, userEmail);
  }

  @Override
  @Async
  public void logPaymentSuccess(Long orderId, String transactionId, String userEmail, String ipAddress) {
    String description = String.format("Pago exitoso para pedido #%d - Transacción: %s", orderId, transactionId);

    Map<String, Object> details = new HashMap<>();
    details.put("orderId", orderId);
    details.put("transactionId", transactionId);
    details.put("paymentStatus", "SUCCESS");

    LogDocument logDoc = createLogDocument("PAYMENT_SUCCESS", userEmail, ipAddress, description,
        "INFO", "PAYMENT", details);
    logDoc.setPostgresReferenceId(orderId);

    saveLogAsync(logDoc);
    log.info("Payment success logged: #{} transaction {} by {}", orderId, transactionId, userEmail);
  }

  @Override
  @Async
  public void logPaymentFailed(Long orderId, String errorReason, String userEmail, String ipAddress) {
    String description = String.format("Pago fallido para pedido #%d - Error: %s", orderId, errorReason);

    Map<String, Object> details = new HashMap<>();
    details.put("orderId", orderId);
    details.put("errorReason", errorReason);
    details.put("paymentStatus", "FAILED");

    LogDocument logDoc = createLogDocument("PAYMENT_FAILED", userEmail, ipAddress, description,
        "ERROR", "PAYMENT", details);
    logDoc.setPostgresReferenceId(orderId);

    saveLogAsync(logDoc);
    log.error("Payment failure logged: #{} error {} by {}", orderId, errorReason, userEmail);
  }

  // ===== IMPLEMENTACIÓN DE EVENTOS DE SISTEMA =====

  @Override
  @Async
  public void logApiAccess(String endpoint, String method, String userEmail, String ipAddress, int responseCode) {
    String description = String.format("API Access: %s %s - Response: %d", method, endpoint, responseCode);

    Map<String, Object> details = new HashMap<>();
    details.put("endpoint", endpoint);
    details.put("httpMethod", method);
    details.put("responseCode", responseCode);

    String level = responseCode >= 400 ? "WARN" : "INFO";

    LogDocument logDoc = createLogDocument("API_ACCESS", userEmail, ipAddress, description,
        level, "API", details);
    saveLogAsync(logDoc);
    log.debug("API access logged: {} {} by {} - Response: {}", method, endpoint, userEmail, responseCode);
  }

  @Override
  @Async
  public void logSuspiciousActivity(String activity, String userEmail, String ipAddress, Map<String, Object> details) {
    String description = String.format("Actividad sospechosa detectada: %s por %s", activity, userEmail);

    LogDocument logDoc = createLogDocument("SUSPICIOUS_ACTIVITY", userEmail, ipAddress, description,
        "WARN", "SECURITY", details);
    saveLogAsync(logDoc);
    log.warn("Suspicious activity logged: {} by {} from {}", activity, userEmail, ipAddress);
  }

  @Override
  @Async
  public void logUnauthorizedAccess(String resource, String userEmail, String ipAddress) {
    String description = String.format("Acceso no autorizado a %s por %s", resource, userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("resource", resource);
    details.put("accessDenied", true);

    LogDocument logDoc = createLogDocument("UNAUTHORIZED_ACCESS", userEmail, ipAddress, description,
        "WARN", "SECURITY", details);
    saveLogAsync(logDoc);
    log.warn("Unauthorized access logged: {} by {} from {}", resource, userEmail, ipAddress);
  }

  @Override
  @Async
  public void logSystemError(String errorMessage, String stackTrace, String module) {
    String description = String.format("Error del sistema en %s: %s", module, errorMessage);

    Map<String, Object> details = new HashMap<>();
    details.put("errorMessage", errorMessage);
    details.put("stackTrace", stackTrace);
    details.put("module", module);

    LogDocument logDoc = createLogDocument("SYSTEM_ERROR", null, null, description,
        "ERROR", module.toUpperCase(), details);
    saveLogAsync(logDoc);
    log.error("System error logged in {}: {}", module, errorMessage);
  }

  @Override
  @Async
  public void logDataExport(String dataType, String userEmail, String ipAddress) {
    String description = String.format("Exportación de datos: %s por %s", dataType, userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("dataType", dataType);
    details.put("exportedBy", userEmail);

    LogDocument logDoc = createLogDocument("DATA_EXPORT", userEmail, ipAddress, description,
        "INFO", "ADMIN", details);
    saveLogAsync(logDoc);
    log.info("Data export logged: {} by {}", dataType, userEmail);
  }

  @Override
  @Async
  public void logQuickSearch(String query, int resultCount, String userEmail, String ipAddress) {
    String description = String.format("Búsqueda rápida: '%s' - %d resultados", query, resultCount);

    Map<String, Object> details = new HashMap<>();
    details.put("query", query);
    details.put("resultCount", resultCount);
    details.put("searchType", "QUICK");

    LogDocument logDoc = createLogDocument("QUICK_SEARCH", userEmail, ipAddress, description,
        "DEBUG", "SEARCH", details);
    saveLogAsync(logDoc);
    log.debug("Quick search logged: '{}' with {} results by {}", query, resultCount, userEmail);
  }

  @Override
  @Async
  public void logSearchQuery(String query, String category, String userEmail, String ipAddress) {
    String description = String.format("Búsqueda: '%s'%s", query,
        category != null ? " en categoría '" + category + "'" : "");

    Map<String, Object> details = new HashMap<>();
    details.put("query", query);
    details.put("category", category);
    details.put("searchType", "FULL");

    LogDocument logDoc = createLogDocument("SEARCH_QUERY", userEmail, ipAddress, description,
        "INFO", "SEARCH", details);
    saveLogAsync(logDoc);
    log.debug("Search query logged: '{}' in category '{}' by {}", query, category, userEmail);
  }

  @Override
  @Async
  public void logPageView(String page, String userEmail, String ipAddress) {
    String description = String.format("Vista de página: %s", page);

    Map<String, Object> details = new HashMap<>();
    details.put("page", page);
    details.put("viewType", "PAGE_ACCESS");

    LogDocument logDoc = createLogDocument("PAGE_VIEW", userEmail, ipAddress, description,
        "DEBUG", "WEB", details);
    saveLogAsync(logDoc);
    log.debug("Page view logged: {} by {}", page, userEmail);
  }

  @Override
  @Async
  public void logPageNotFound(String requestUri, String userEmail, String ipAddress) {
    String description = String.format("Página no encontrada: %s", requestUri);

    Map<String, Object> details = new HashMap<>();
    details.put("requestUri", requestUri);
    details.put("errorType", "404_NOT_FOUND");

    LogDocument logDoc = createLogDocument("PAGE_NOT_FOUND", userEmail, ipAddress, description,
        "WARN", "WEB", details);
    saveLogAsync(logDoc);
    log.warn("Page not found logged: {} accessed by {}", requestUri, userEmail);
  }

  @Override
  @Async
  public void logDashboardAccess(String userEmail, String ipAddress) {
    String description = String.format("Acceso al dashboard por %s", userEmail);

    LogDocument logDoc = createLogDocument("DASHBOARD_ACCESS", userEmail, ipAddress, description,
        "INFO", "DASHBOARD", null);
    saveLogAsync(logDoc);
    log.debug("Dashboard access logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logAdminDashboardAccess(String userEmail, String ipAddress) {
    String description = String.format("Acceso al panel administrativo por %s", userEmail);

    Map<String, Object> details = new HashMap<>();
    details.put("accessType", "ADMIN_PANEL");
    details.put("privilegeLevel", "ADMIN");

    LogDocument logDoc = createLogDocument("ADMIN_DASHBOARD_ACCESS", userEmail, ipAddress, description,
        "WARN", "ADMIN", details);
    saveLogAsync(logDoc);
    log.info("Admin dashboard access logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logProfileUpdate(String userEmail, String ipAddress) {
    String description = String.format("Perfil actualizado por %s", userEmail);

    LogDocument logDoc = createLogDocument("PROFILE_UPDATED", userEmail, ipAddress, description,
        "INFO", "USER", null);
    saveLogAsync(logDoc);
    log.info("Profile update logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logNotificationPreferencesUpdate(String userEmail, String ipAddress) {
    String description = String.format("Preferencias de notificación actualizadas por %s", userEmail);

    LogDocument logDoc = createLogDocument("NOTIFICATION_PREFERENCES_UPDATED", userEmail, ipAddress,
        description, "INFO", "USER", null);
    saveLogAsync(logDoc);
    log.debug("Notification preferences update logged for: {}", userEmail);
  }

  @Override
  @Async
  public void logContactFormSubmission(String name, String email, String subject, String ipAddress) {
    String description = String.format("Formulario de contacto enviado por %s (%s): %s", name, email, subject);

    Map<String, Object> details = new HashMap<>();
    details.put("senderName", name);
    details.put("senderEmail", email);
    details.put("subject", subject);
    details.put("formType", "CONTACT");

    LogDocument logDoc = createLogDocument("CONTACT_FORM_SUBMITTED", email, ipAddress, description,
        "INFO", "WEB", details);
    saveLogAsync(logDoc);
    log.info("Contact form submission logged: {} ({})", name, email);
  }

  // ===== MÉTODOS DE CONSULTA =====

  @Override
  public List<LogDocument> getLogsByUser(String userEmail, LocalDateTime startDate, LocalDateTime endDate) {
    return logRepository.findUserLogsInDateRange(userEmail, startDate, endDate);
  }

  @Override
  public List<LogDocument> getLogsByEventType(String eventType, LocalDateTime startDate, LocalDateTime endDate) {
    return logRepository.findByEventTypeAndTimestampBetween(eventType, startDate, endDate);
  }

  @Override
  public Page<LogDocument> getSecurityLogs(Pageable pageable) {
    return logRepository.findSecurityLogs(pageable);
  }

  @Override
  public Page<LogDocument> getErrorLogs(Pageable pageable) {
    return logRepository.findErrorLogs(pageable);
  }

  @Override
  public List<LogDocument> getAuthenticationLogs(LocalDateTime startDate, LocalDateTime endDate) {
    return logRepository.findAuthenticationLogs();
  }

  @Override
  public List<LogDocument> getFailedLoginAttempts(String userEmail, LocalDateTime since) {
    return logRepository.findFailedLoginAttemptsInTimeRange(since, LocalDateTime.now());
  }

  @Override
  public List<LogDocument> getFailedLoginAttemptsByIp(String ipAddress, LocalDateTime since) {
    return logRepository.findFailedLoginAttemptsByIp(ipAddress);
  }

  @Override
  public List<LogDocument> getRecentLogs(int limit) {
    return logRepository.findTop100ByOrderByTimestampDesc();
  }

  @Override
  public long countFailedLoginAttempts(String userEmail, LocalDateTime since) {
    return logRepository.countFailedLoginAttempts(userEmail, since);
  }

  @Override
  public long countFailedLoginAttemptsByIp(String ipAddress, LocalDateTime since) {
    return logRepository.countFailedLoginAttemptsByIp(ipAddress, since);
  }

  @Override
  public List<LogDocument> searchLogs(String searchTerm, LocalDateTime startDate, LocalDateTime endDate) {
    return logRepository.findByDescriptionContaining(searchTerm);
  }

  @Override
  public Map<String, Long> getLogStatistics(LocalDateTime startDate, LocalDateTime endDate) {
    Map<String, Long> stats = new HashMap<>();

    List<LogDocument> logs = logRepository.findByTimestampBetween(startDate, endDate);

    // Contar por tipo de evento
    Map<String, Long> eventTypeCounts = new HashMap<>();
    Map<String, Long> levelCounts = new HashMap<>();
    Map<String, Long> moduleCounts = new HashMap<>();

    for (LogDocument log : logs) {
      // Por tipo de evento
      eventTypeCounts.merge(log.getEventType(), 1L, Long::sum);

      // Por nivel
      levelCounts.merge(log.getLevel(), 1L, Long::sum);

      // Por módulo
      if (log.getModule() != null) {
        moduleCounts.merge(log.getModule(), 1L, Long::sum);
      }
    }

    stats.put("total_logs", (long) logs.size());
    stats.put("error_logs", levelCounts.getOrDefault("ERROR", 0L));
    stats.put("warning_logs", levelCounts.getOrDefault("WARN", 0L));
    stats.put("info_logs", levelCounts.getOrDefault("INFO", 0L));
    stats.put("login_attempts", eventTypeCounts.getOrDefault("LOGIN_SUCCESS", 0L) +
        eventTypeCounts.getOrDefault("LOGIN_FAILED", 0L));
    stats.put("failed_logins", eventTypeCounts.getOrDefault("LOGIN_FAILED", 0L));

    return stats;
  }

  @Override
  public List<LogDocument> getUserActivity(String userEmail, LocalDateTime startDate, LocalDateTime endDate) {
    return logRepository.findUserLogsInDateRange(userEmail, startDate, endDate);
  }

  @Override
  public List<LogDocument> getLogsByModule(String module, LocalDateTime startDate, LocalDateTime endDate) {
    return logRepository.findByModule(module);
  }

  @Override
  public boolean hasSuspiciousActivityRecent(String userEmail, int hoursBack) {
    LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
    List<LogDocument> suspiciousLogs = logRepository.findByEventTypeAndUserEmail("SUSPICIOUS_ACTIVITY", userEmail);

    return suspiciousLogs.stream()
        .anyMatch(log -> log.getTimestamp().isAfter(since));
  }

  @Override
  public boolean isIpSuspicious(String ipAddress, int hoursBack, int maxAttempts) {
    LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
    long failedAttempts = logRepository.countFailedLoginAttemptsByIp(ipAddress, since);

    return failedAttempts >= maxAttempts;
  }

  // ===== MÉTODOS DE ADMINISTRACIÓN =====

  @Override
  @Async
  public void cleanupOldLogs(int daysToKeep) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);

    // En MongoDB, eliminar directamente logs antiguos
    // Nota: Implementar con cuidado en producción, considerar archivado
    log.info("Cleaning up logs older than {} days", daysToKeep);

    // Por ahora, solo log la operación
    logInfo("LOG_CLEANUP",
        String.format("Limpieza de logs anterior a %s", cutoffDate),
        "SYSTEM", null);
  }

  @Override
  @Async
  public void archiveOldLogs(int daysToArchive) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToArchive);
    log.info("Archiving logs older than {} days", daysToArchive);

    // Implementar lógica de archivado
    logInfo("LOG_ARCHIVE",
        String.format("Archivado de logs anterior a %s", cutoffDate),
        "SYSTEM", null);
  }

  @Override
  public String exportLogsToFile(LocalDateTime startDate, LocalDateTime endDate, String format) {
    log.info("Exporting logs from {} to {} in format {}", startDate, endDate, format);

    // Implementar exportación
    logInfo("LOG_EXPORT",
        String.format("Exportación de logs desde %s hasta %s en formato %s", startDate, endDate, format),
        "SYSTEM", null);

    return "logs_export_" + System.currentTimeMillis() + "." + format.toLowerCase();
  }

  @Override
  public Map<String, Object> getSystemMetrics() {
    Map<String, Object> metrics = new HashMap<>();

    LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
    LocalDateTime lastHour = LocalDateTime.now().minusHours(1);

    metrics.put("total_logs_24h", logRepository.countByTimestampBetween(last24Hours, LocalDateTime.now()));
    metrics.put("error_logs_24h", logRepository.countErrorLogsLast24Hours(last24Hours));
    metrics.put("failed_logins_24h", logRepository.countByEventType("LOGIN_FAILED"));
    metrics.put("successful_logins_24h", logRepository.countByEventType("LOGIN_SUCCESS"));
    metrics.put("api_calls_1h", logRepository.countByEventType("API_ACCESS"));

    return metrics;
  }

  @Override
  public Map<String, Object> generateSecurityReport(LocalDateTime startDate, LocalDateTime endDate) {
    Map<String, Object> report = new HashMap<>();

    List<LogDocument> securityLogs = logRepository.findSecurityLogs();
    List<LogDocument> failedLogins = logRepository.findFailedLoginAttemptsInTimeRange(startDate, endDate);
    List<LogDocument> suspiciousActivity = logRepository.findSuspiciousActivity();

    report.put("period_start", startDate);
    report.put("period_end", endDate);
    report.put("total_security_events", securityLogs.size());
    report.put("failed_login_attempts", failedLogins.size());
    report.put("suspicious_activities", suspiciousActivity.size());

    // Análisis por IP
    Map<String, Long> ipFailures = new HashMap<>();
    for (LogDocument log : failedLogins) {
      if (log.getIpAddress() != null) {
        ipFailures.merge(log.getIpAddress(), 1L, Long::sum);
      }
    }
    report.put("top_failed_ips", ipFailures);

    return report;
  }

  @Override
  public Map<String, Object> generateUserActivityReport(LocalDateTime startDate, LocalDateTime endDate) {
    Map<String, Object> report = new HashMap<>();

    List<LogDocument> logs = logRepository.findByTimestampBetween(startDate, endDate);

    Map<String, Long> userActivities = new HashMap<>();
    Map<String, Long> moduleActivities = new HashMap<>();

    for (LogDocument log : logs) {
      if (log.getUserEmail() != null) {
        userActivities.merge(log.getUserEmail(), 1L, Long::sum);
      }
      if (log.getModule() != null) {
        moduleActivities.merge(log.getModule(), 1L, Long::sum);
      }
    }

    report.put("period_start", startDate);
    report.put("period_end", endDate);
    report.put("total_activities", logs.size());
    report.put("unique_users", userActivities.size());
    report.put("user_activities", userActivities);
    report.put("module_activities", moduleActivities);

    return report;
  }

  // ===== MÉTODOS DE LOGGING GENÉRICO =====

  @Override
  @Async
  public void logEvent(String eventType, String description, String userEmail, String ipAddress,
      String level, String module, Map<String, Object> details) {
    LogDocument logDoc = createLogDocument(eventType, userEmail, ipAddress, description, level, module, details);
    saveLogAsync(logDoc);
  }

  @Override
  @Async
  public void logInfo(String eventType, String description, String userEmail, String ipAddress) {
    logEvent(eventType, description, userEmail, ipAddress, "INFO", "GENERAL", null);
  }

  @Override
  @Async
  public void logWarning(String eventType, String description, String userEmail, String ipAddress) {
    logEvent(eventType, description, userEmail, ipAddress, "WARN", "GENERAL", null);
  }

  @Override
  @Async
  public void logError(String eventType, String description, String userEmail, String ipAddress,
      Map<String, Object> errorDetails) {
    logEvent(eventType, description, userEmail, ipAddress, "ERROR", "GENERAL", errorDetails);
  }

  @Override
  @Async
  public void logDebug(String eventType, String description, Map<String, Object> details) {
    logEvent(eventType, description, null, null, "DEBUG", "GENERAL", details);
  }

  // ===== MÉTODOS AUXILIARES PRIVADOS =====

  /**
   * PATRÓN FACTORY: Factory method para crear LogDocument
   */
  private LogDocument createLogDocument(String eventType, String userEmail, String ipAddress,
      String description, String level, String module,
      Map<String, Object> details) {
    LogDocument.LogDocumentBuilder builder = LogDocument.builder()
        .timestamp(LocalDateTime.now())
        .eventType(eventType)
        .userEmail(userEmail)
        .ipAddress(ipAddress)
        .description(description)
        .level(level)
        .module(module);

    if (details != null) {
      builder.details(details);
    }

    // Agregar información adicional del contexto
    if (details == null) {
      details = new HashMap<>();
    }
    details.put("server", "proyecto-final-utp");
    details.put("environment", getEnvironment());

    builder.details(details);

    return builder.build();
  }

  /**
   * PATRÓN ASYNC: Guardar log de forma asíncrona
   */
  @Async
  private void saveLogAsync(LogDocument logDocument) {
    try {
      logRepository.save(logDocument);
    } catch (Exception e) {
      // Log local en caso de error de MongoDB
      log.error("Error guardando log en MongoDB: {}", e.getMessage());
    }
  }

  /**
   * Contar intentos fallidos recientes para un usuario
   */
  private int countRecentFailedAttempts(String email) {
    LocalDateTime since = LocalDateTime.now().minusHours(1);
    return (int) logRepository.countFailedLoginAttempts(email, since);
  }

  /**
   * Obtener User-Agent actual (placeholder)
   */
  private String getCurrentUserAgent() {
    // Implementar obtención del User-Agent del request actual
    return "Unknown";
  }

  /**
   * Obtener environment actual
   */
  private String getEnvironment() {
    return System.getProperty("spring.profiles.active", "development");
  }
}