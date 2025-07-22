package com.utp.proyectofinal.models.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Documento Log para MongoDB - Registro de actividad del sistema
 * 
 * PATRONES IMPLEMENTADOS:
 * - DOCUMENT: Patrón Document de MongoDB
 * - BUILDER: Patrón Builder con Lombok
 * - OBSERVER: Registro de eventos del sistema
 * - MEMENTO: Almacena el estado de eventos para auditoría
 * 
 * @author Julio Pariona
 */
@Document(collection = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogDocument {

  @Id
  private String id;

  @NotNull
  @Indexed
  private LocalDateTime timestamp;

  @NotBlank
  @Field("event_type")
  @Indexed
  private String eventType;

  @Field("user_id")
  @Indexed
  private String userId; // ObjectId del usuario en MongoDB

  @Field("user_email")
  @Indexed
  private String userEmail;

  @NotBlank
  private String description;

  @Field("ip_address")
  private String ipAddress;

  private Map<String, Object> details;

  // Campos adicionales para mejor tracking
  @Field("session_id")
  private String sessionId;

  @Field("user_agent")
  private String userAgent;

  private String module; // Módulo del sistema (AUTH, PRODUCTS, ORDERS, etc.)

  private String level; // INFO, WARN, ERROR, DEBUG

  @Field("postgres_reference_id")
  private Long postgresReferenceId; // Referencia a entidad de PostgreSQL si aplica

  /**
   * Enumeración de tipos de eventos
   */
  public enum EventType {
    // Autenticación
    LOGIN_SUCCESS("LOGIN_SUCCESS"),
    LOGIN_FAILED("LOGIN_FAILED"),
    LOGOUT("LOGOUT"),
    PASSWORD_CHANGE("PASSWORD_CHANGE"),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED"),

    // Usuarios
    USER_CREATED("USER_CREATED"),
    USER_UPDATED("USER_UPDATED"),
    USER_DELETED("USER_DELETED"),
    EMAIL_VERIFIED("EMAIL_VERIFIED"),

    // Productos
    PRODUCT_CREATED("PRODUCT_CREATED"),
    PRODUCT_UPDATED("PRODUCT_UPDATED"),
    PRODUCT_DELETED("PRODUCT_DELETED"),
    PRODUCT_VIEWED("PRODUCT_VIEWED"),

    // Carrito y Pedidos
    CART_ITEM_ADDED("CART_ITEM_ADDED"),
    CART_ITEM_REMOVED("CART_ITEM_REMOVED"),
    ORDER_CREATED("ORDER_CREATED"),
    ORDER_UPDATED("ORDER_UPDATED"),
    ORDER_CANCELLED("ORDER_CANCELLED"),

    // Pagos
    PAYMENT_INITIATED("PAYMENT_INITIATED"),
    PAYMENT_SUCCESS("PAYMENT_SUCCESS"),
    PAYMENT_FAILED("PAYMENT_FAILED"),

    // Sistema
    SYSTEM_ERROR("SYSTEM_ERROR"),
    API_ACCESS("API_ACCESS"),
    DATA_EXPORT("DATA_EXPORT"),

    // Seguridad
    SUSPICIOUS_ACTIVITY("SUSPICIOUS_ACTIVITY"),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS");

    private final String value;

    EventType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /**
     * PATRÓN FACTORY: Factory method para crear desde string
     */
    public static EventType fromString(String eventType) {
      for (EventType e : EventType.values()) {
        if (e.getValue().equalsIgnoreCase(eventType)) {
          return e;
        }
      }
      throw new IllegalArgumentException("Tipo de evento no válido: " + eventType);
    }
  }

  /**
   * Enumeración de niveles de log
   */
  public enum LogLevel {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");

    private final String value;

    LogLevel(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * PATRÓN FACTORY: Factory method para crear log de autenticación
   */
  public static LogDocument createAuthLog(String eventType, String userEmail, String ipAddress, boolean success) {
    return LogDocument.builder()
        .timestamp(LocalDateTime.now())
        .eventType(eventType)
        .userEmail(userEmail)
        .ipAddress(ipAddress)
        .description(success ? "Autenticación exitosa" : "Falló la autenticación")
        .level(success ? LogLevel.INFO.getValue() : LogLevel.WARN.getValue())
        .module("AUTH")
        .build();
  }

  /**
   * PATRÓN FACTORY: Factory method para crear log de sistema
   */
  public static LogDocument createSystemLog(String eventType, String description, LogLevel level) {
    return LogDocument.builder()
        .timestamp(LocalDateTime.now())
        .eventType(eventType)
        .description(description)
        .level(level.getValue())
        .module("SYSTEM")
        .build();
  }

  /**
   * PATRÓN FACTORY: Factory method para crear log de usuario
   */
  public static LogDocument createUserLog(String eventType, String userId, String userEmail, String description) {
    return LogDocument.builder()
        .timestamp(LocalDateTime.now())
        .eventType(eventType)
        .userId(userId)
        .userEmail(userEmail)
        .description(description)
        .level(LogLevel.INFO.getValue())
        .module("USER")
        .build();
  }

  /**
   * Método para agregar detalles adicionales
   */
  public void addDetail(String key, Object value) {
    if (details == null) {
      details = new java.util.HashMap<>();
    }
    details.put(key, value);
  }

  /**
   * Método para verificar si es un evento de error
   */
  public boolean isError() {
    return LogLevel.ERROR.getValue().equals(level);
  }

  /**
   * Método para verificar si es un evento de seguridad
   */
  public boolean isSecurityEvent() {
    return eventType.contains("LOGIN") ||
        eventType.contains("UNAUTHORIZED") ||
        eventType.contains("SUSPICIOUS");
  }

  /**
   * toString personalizado para logging
   */
  @Override
  public String toString() {
    return "LogDocument{" +
        "timestamp=" + timestamp +
        ", eventType='" + eventType + '\'' +
        ", userEmail='" + userEmail + '\'' +
        ", level='" + level + '\'' +
        ", module='" + module + '\'' +
        ", description='" + description + '\'' +
        '}';
  }
}