package com.utp.proyectofinal.models.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respuesta de perfil de usuario
 * 
 * PATRONES IMPLEMENTADOS:
 * - DTO: Data Transfer Object para respuesta
 * - BUILDER: Patrón Builder con Lombok
 * - COMPOSITE: Combina datos de PostgreSQL y MongoDB
 * 
 * @author Julio Pariona
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    // Datos básicos del usuario
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String address;

    // Información de estado
    private Boolean enabled;
    private Boolean locked;
    private Boolean verified;
    private String status;

    // Información de rol
    private String roleName;
    private String roleDisplayName;

    // Fechas importantes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private LocalDateTime registrationDate;

    // Datos extendidos de MongoDB
    private String username;
    private AddressDetails addressDetails;
    private NotificationPreferences notificationPreferences;
    private Map<String, Object> privacySettings;
    private Map<String, Object> metadata;

    // Estadísticas del usuario
    private UserStatistics statistics;

    /**
     * VALUE OBJECT: Detalles de dirección
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDetails {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        
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
        @Builder.Default
        private Boolean emailAlerts = true;
        
        @Builder.Default
        private Boolean inAppNotifications = true;
        
        @Builder.Default
        private Boolean marketingEmails = false;
        
        @Builder.Default
        private Boolean orderUpdates = true;
        
        @Builder.Default
        private Boolean promotionalOffers = false;
        
        @Builder.Default
        private Boolean securityAlerts = true;
    }

    /**
     * VALUE OBJECT: Estadísticas del usuario
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserStatistics {
        @Builder.Default
        private Long totalOrders = 0L;
        
        @Builder.Default
        private Double totalSpent = 0.0;
        
        @Builder.Default
        private Long loginCount = 0L;
        
        @Builder.Default
        private Integer productViews = 0;
        
        @Builder.Default
        private Integer cartItems = 0;
        
        @Builder.Default
        private Double averageOrderValue = 0.0;
        
        private String preferredCategory;
        private LocalDateTime lastOrderDate;
        private String favoriteProduct;
    }

    /**
     * Método para obtener nombre completo
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Método para verificar si el perfil está completo
     */
    public boolean isProfileComplete() {
        return firstName != null && lastName != null && 
               email != null && phone != null && 
               birthDate != null && verified;
    }

    /**
     * Método para obtener antigüedad de la cuenta en días
     */
    public long getAccountAgeInDays() {
        if (createdAt == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDate.now());
    }

    /**
     * Método para verificar si es usuario activo (login reciente)
     */
    public boolean isActiveUser() {
        if (lastLogin == null) return false;
        return lastLogin.isAfter(LocalDateTime.now().minusDays(30));
    }

    /**
     * Método para obtener nivel de usuario basado en gastos
     */
    public String getUserLevel() {
        if (statistics == null || statistics.totalSpent == null) {
            return "NUEVO";
        }
        
        double totalSpent = statistics.totalSpent;
        if (totalSpent >= 5000) return "VIP";
        if (totalSpent >= 2000) return "PREMIUM";
        if (totalSpent >= 500) return "SILVER";
        if (totalSpent > 0) return "BRONZE";
        return "NUEVO";
    }

    /**
     * PATRÓN FACTORY: Factory method para crear respuesta básica
     */
    public static UserProfileResponse createBasicProfile(Long id, String firstName, String lastName, 
                                                        String email, String roleName) {
        return UserProfileResponse.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .roleName(roleName)
                .statistics(UserStatistics.builder().build())
                .notificationPreferences(NotificationPreferences.builder().build())
                .build();
    }

    /**
     * toString personalizado para logging
     */
    @Override
    public String toString() {
        return "UserProfileResponse{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", roleName='" + roleName + '\'' +
                ", verified=" + verified +
                ", active=" + isActiveUser() +
                ", level='" + getUserLevel() + '\'' +
                '}';
    }
}