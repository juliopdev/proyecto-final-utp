package com.utp.proyectofinal.services.interfaces;

import java.util.List;
import java.util.Map;

/**
 * Interface para el servicio de envío de emails
 * 
 * PATRONES IMPLEMENTADOS:
 * - STRATEGY: Diferentes estrategias de envío de email
 * - TEMPLATE METHOD: Templates para diferentes tipos de emails
 * - COMMAND: Cada método encapsula un comando de envío
 * - FACTORY: Factory methods para crear diferentes tipos de emails
 * 
 * @author Julio Pariona
 */
public interface EmailService {

    // ===== EMAILS DE AUTENTICACIÓN =====

    /**
     * PATRÓN TEMPLATE METHOD: Template para email de verificación
     */
    void sendVerificationEmail(String email, String userName, String verificationToken);

    /**
     * PATRÓN TEMPLATE METHOD: Template para email de bienvenida
     */
    void sendWelcomeEmail(String email, String userName);

    /**
     * PATRÓN TEMPLATE METHOD: Template para email de reset de contraseña
     */
    void sendPasswordResetEmail(String email, String userName, String resetToken);

    /**
     * PATRÓN TEMPLATE METHOD: Template para confirmación de cambio de contraseña
     */
    void sendPasswordChangeConfirmationEmail(String email, String userName);

    /**
     * PATRÓN TEMPLATE METHOD: Template para notificación de login sospechoso
     */
    void sendSuspiciousLoginEmail(String email, String userName, String ipAddress, String location);

    // ===== EMAILS DE PEDIDOS Y COMERCIO =====

    /**
     * PATRÓN TEMPLATE METHOD: Template para confirmación de pedido
     */
    void sendOrderConfirmationEmail(String email, String userName, Long orderId, 
                                   Double totalAmount, List<Map<String, Object>> orderItems);

    /**
     * PATRÓN TEMPLATE METHOD: Template para actualización de estado de pedido
     */
    void sendOrderStatusUpdateEmail(String email, String userName, Long orderId, 
                                   String newStatus, String estimatedDelivery);

    /**
     * PATRÓN TEMPLATE METHOD: Template para cancelación de pedido
     */
    void sendOrderCancellationEmail(String email, String userName, Long orderId, String reason);

    /**
     * PATRÓN TEMPLATE METHOD: Template para confirmación de pago
     */
    void sendPaymentConfirmationEmail(String email, String userName, Long orderId, 
                                     String transactionId, Double amount);

    /**
     * PATRÓN TEMPLATE METHOD: Template para fallo de pago
     */
    void sendPaymentFailedEmail(String email, String userName, Long orderId, String reason);

    /**
     * PATRÓN TEMPLATE METHOD: Template para recordatorio de carrito abandonado
     */
    void sendAbandonedCartEmail(String email, String userName, List<Map<String, Object>> cartItems);

    // ===== EMAILS DE MARKETING =====

    /**
     * PATRÓN TEMPLATE METHOD: Template para newsletter
     */
    void sendNewsletterEmail(String email, String userName, String subject, String content);

    /**
     * PATRÓN TEMPLATE METHOD: Template para promociones especiales
     */
    void sendPromotionalEmail(String email, String userName, String promotionTitle, 
                             String discountCode, Double discountPercentage);

    /**
     * PATRÓN TEMPLATE METHOD: Template para nuevos productos
     */
    void sendNewProductAnnouncementEmail(String email, String userName, 
                                        List<Map<String, Object>> newProducts);

    /**
     * PATRÓN COMMAND: Envío masivo de emails promocionales
     */
    void sendBulkPromotionalEmail(List<String> emails, String subject, String content, 
                                 Map<String, String> personalizations);

    // ===== EMAILS ADMINISTRATIVOS =====

    /**
     * PATRÓN TEMPLATE METHOD: Template para notificación a administradores
     */
    void sendAdminNotificationEmail(String subject, String message, Map<String, Object> data);

    /**
     * PATRÓN TEMPLATE METHOD: Template para reportes del sistema
     */
    void sendSystemReportEmail(String reportType, Map<String, Object> reportData, 
                              List<String> adminEmails);

    /**
     * PATRÓN TEMPLATE METHOD: Template para alertas de seguridad
     */
    void sendSecurityAlertEmail(String alertType, String description, 
                               String affectedUser, List<String> adminEmails);

    /**
     * PATRÓN TEMPLATE METHOD: Template para notificación de nuevo registro
     */
    void sendNewUserRegistrationNotification(String newUserEmail, String newUserName, 
                                            List<String> adminEmails);

    // ===== EMAILS DE SOPORTE =====

    /**
     * PATRÓN TEMPLATE METHOD: Template para confirmación de ticket de soporte
     */
    void sendSupportTicketConfirmationEmail(String email, String userName, String ticketId, 
                                           String subject, String description);

    /**
     * PATRÓN TEMPLATE METHOD: Template para respuesta de soporte
     */
    void sendSupportResponseEmail(String email, String userName, String ticketId, 
                                 String response, String supportAgent);

    /**
     * PATRÓN TEMPLATE METHOD: Template para encuesta de satisfacción
     */
    void sendSatisfactionSurveyEmail(String email, String userName, String surveyUrl);

    // ===== EMAILS TRANSACCIONALES =====

    /**
     * PATRÓN TEMPLATE METHOD: Template para factura
     */
    void sendInvoiceEmail(String email, String userName, String invoiceNumber, 
                         Double amount, byte[] invoicePdf);

    /**
     * PATRÓN TEMPLATE METHOD: Template para recibo de pago
     */
    void sendReceiptEmail(String email, String userName, String receiptNumber, 
                         Map<String, Object> paymentDetails);

    /**
     * PATRÓN TEMPLATE METHOD: Template para devolución/reembolso
     */
    void sendRefundNotificationEmail(String email, String userName, String refundId, 
                                    Double refundAmount, String reason);

    // ===== MÉTODOS DE CONFIGURACIÓN Y UTILIDAD =====

    /**
     * PATRÓN STRATEGY: Validar formato de email
     */
    boolean isValidEmail(String email);

    /**
     * PATRÓN COMMAND: Enviar email personalizado
     */
    void sendCustomEmail(String to, String subject, String content, 
                        Map<String, Object> templateVariables);

    /**
     * PATRÓN COMMAND: Enviar email con plantilla
     */
    void sendTemplatedEmail(String to, String templateName, Map<String, Object> variables);

    /**
     * PATRÓN COMMAND: Enviar email con archivos adjuntos
     */
    void sendEmailWithAttachments(String to, String subject, String content, 
                                 List<Map<String, Object>> attachments);

    /**
     * PATRÓN STRATEGY: Verificar estado de entrega de email
     */
    Map<String, Object> getEmailDeliveryStatus(String emailId);

    /**
     * PATRÓN COMMAND: Obtener estadísticas de envío
     */
    Map<String, Long> getEmailStatistics();

    /**
     * PATRÓN STRATEGY: Verificar si email está en lista negra
     */
    boolean isEmailBlacklisted(String email);

    /**
     * PATRÓN COMMAND: Agregar email a lista negra
     */
    void addEmailToBlacklist(String email, String reason);

    /**
     * PATRÓN COMMAND: Remover email de lista negra
     */
    void removeEmailFromBlacklist(String email);

    // ===== MÉTODOS DE GESTIÓN DE PLANTILLAS =====

    /**
     * PATRÓN FACTORY: Crear nueva plantilla de email
     */
    void createEmailTemplate(String templateName, String subject, String htmlContent, 
                            String textContent, Map<String, String> defaultVariables);

    /**
     * PATRÓN COMMAND: Actualizar plantilla existente
     */
    void updateEmailTemplate(String templateName, String subject, String htmlContent, 
                            String textContent);

    /**
     * PATRÓN COMMAND: Eliminar plantilla
     */
    void deleteEmailTemplate(String templateName);

    /**
     * PATRÓN STRATEGY: Obtener lista de plantillas disponibles
     */
    List<Map<String, Object>> getAvailableTemplates();

    // ===== MÉTODOS DE PROGRAMACIÓN DE ENVÍOS =====

    /**
     * PATRÓN COMMAND: Programar envío de email
     */
    String scheduleEmail(String to, String subject, String content, 
                        java.time.LocalDateTime scheduledTime);

    /**
     * PATRÓN COMMAND: Cancelar email programado
     */
    void cancelScheduledEmail(String emailId);

    /**
     * PATRÓN STRATEGY: Obtener emails programados
     */
    List<Map<String, Object>> getScheduledEmails();

    // ===== MÉTODOS DE CONFIGURACIÓN DE PREFERENCIAS =====

    /**
     * PATRÓN COMMAND: Configurar preferencias de email para usuario
     */
    void setUserEmailPreferences(String email, Map<String, Boolean> preferences);

    /**
     * PATRÓN STRATEGY: Obtener preferencias de email del usuario
     */
    Map<String, Boolean> getUserEmailPreferences(String email);

    /**
     * PATRÓN STRATEGY: Verificar si usuario acepta tipo de email
     */
    boolean userAcceptsEmailType(String email, String emailType);

    // ===== MÉTODOS DE SEGUIMIENTO Y ANALYTICS =====

    /**
     * PATRÓN COMMAND: Registrar apertura de email
     */
    void trackEmailOpen(String emailId, String userEmail);

    /**
     * PATRÓN COMMAND: Registrar clic en email
     */
    void trackEmailClick(String emailId, String userEmail, String clickedLink);

    /**
     * PATRÓN STRATEGY: Obtener métricas de email
     */
    Map<String, Object> getEmailMetrics(String emailType, 
                                       java.time.LocalDateTime startDate, 
                                       java.time.LocalDateTime endDate);

    /**
     * PATRÓN COMMAND: Generar reporte de rendimiento de emails
     */
    Map<String, Object> generateEmailPerformanceReport(java.time.LocalDateTime startDate, 
                                                       java.time.LocalDateTime endDate);

    // ===== MÉTODOS DE VALIDACIÓN Y LIMPIEZA =====

    /**
     * PATRÓN STRATEGY: Validar lista de emails
     */
    Map<String, Boolean> validateEmailList(List<String> emails);

    /**
     * PATRÓN COMMAND: Limpiar emails rebotados
     */
    int cleanupBouncedEmails();

    /**
     * PATRÓN COMMAND: Procesar desuscripciones
     */
    void processUnsubscription(String email, String reason);

    /**
     * PATRÓN STRATEGY: Verificar estado de suscripción
     */
    boolean isUserSubscribed(String email, String emailType);

    // ===== MÉTODOS DE CONFIGURACIÓN DEL SISTEMA =====

    /**
     * PATRÓN COMMAND: Configurar límites de envío
     */
    void setEmailSendingLimits(int hourlyLimit, int dailyLimit);

    /**
     * PATRÓN STRATEGY: Verificar límites de envío
     */
    boolean canSendEmail(String email);

    /**
     * PATRÓN COMMAND: Configurar servidor SMTP
     */
    void configureSMTPSettings(Map<String, String> smtpConfig);

    /**
     * PATRÓN STRATEGY: Verificar configuración de email
     */
    Map<String, Object> validateEmailConfiguration();
}