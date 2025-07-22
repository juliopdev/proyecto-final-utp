package com.utp.proyectofinal.models.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Documento User para MongoDB - Datos extendidos y comportamiento
 * 
 * PATRONES IMPLEMENTADOS:
 * - DOCUMENT: Patrón Document de MongoDB para almacenamiento NoSQL
 * - BUILDER: Patrón Builder con Lombok
 * - VALUE OBJECT: Address como objeto embebido
 * - AGGREGATE: Agregación de datos relacionados al usuario
 * 
 * Este documento almacena información extendida del usuario que no necesita
 * consistencia ACID pero sí flexibilidad de esquema
 * 
 * @author Julio Pariona
 */
@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument {

    @Id
    private String id; // ObjectId de MongoDB

    @NotBlank
    @Indexed(unique = true)
    private String username;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @Field("password_hash")
    private String passwordHash;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("phone_number")
    private String phoneNumber;

    // VALUE OBJECT embebido
    private Address address;

    private List<String> roles;

    private String status; // ACTIVE, INACTIVE, PENDING_VERIFICATION

    @Field("registration_date")
    private LocalDateTime registrationDate;

    @Field("last_login")
    private LocalDateTime lastLogin;

    @Field("notification_preferences")
    private NotificationPreferences notificationPreferences;

    @Field("is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    // Referencia al ID de PostgreSQL
    @Field("postgres_user_id")
    private Long postgresUserId;

    // Metadatos adicionales
    private Map<String, Object> metadata;

    /**
     * VALUE OBJECT: Address embebido
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        private String street;
        private String city;
        private String state; // Departamento
        @Field("zip_code")
        private String zipCode;
        @Builder.Default
        private String country = "Perú";

        public String getFullAddress() {
            return String.join(", ", street, city, state, country);
        }
    }

    /**
     * VALUE OBJECT: Preferencias de notificación
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationPreferences {
        @Field("email_alerts")
        @Builder.Default
        private Boolean emailAlerts = true;

        @Field("in_app_notifications")
        @Builder.Default
        private Boolean inAppNotifications = true;

        @Field("marketing_emails")
        @Builder.Default
        private Boolean marketingEmails = false;

        @Field("order_updates")
        @Builder.Default
        private Boolean orderUpdates = true;
    }

    /**
     * Método para verificar si el usuario está activo
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Método para actualizar último login
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * Método para verificar si las notificaciones están habilitadas
     */
    public boolean areNotificationsEnabled() {
        return notificationPreferences != null && 
               (notificationPreferences.emailAlerts || notificationPreferences.inAppNotifications);
    }

    /**
     * Método para obtener nombre completo
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Método para agregar metadata
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * toString personalizado para logging (sin password)
     */
    @Override
    public String toString() {
        return "UserDocument{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", isVerified=" + isVerified +
                ", postgresUserId=" + postgresUserId +
                '}';
    }
}