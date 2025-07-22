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
 * DTO para actualización de perfil de usuario
 * 
 * PATRONES IMPLEMENTADOS:
 * - DTO: Data Transfer Object para actualización de perfil
 * - BUILDER: Patrón Builder con Lombok
 * - VALIDATION: Validación con Bean Validation
 * 
 * @author Julio Pariona
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    private String lastName;

    @Pattern(
        regexp = "^\\+?[0-9]{8,15}$", 
        message = "El teléfono debe tener entre 8 y 15 dígitos"
    )
    private String phone;

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate birthDate;

    @Size(max = 222, message = "La dirección no puede exceder 222 caracteres")
    private String address;

    // Campos adicionales para MongoDB
    private String username;
    
    private Address addressDetails;
    
    private Map<String, Boolean> notificationPreferences;
    
    private Map<String, Object> privacySettings;

    /**
     * VALUE OBJECT: Dirección detallada para MongoDB
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        @Size(max = 100, message = "La calle no puede exceder 100 caracteres")
        private String street;
        
        @Size(max = 50, message = "La ciudad no puede exceder 50 caracteres")
        private String city;
        
        @Size(max = 50, message = "El estado no puede exceder 50 caracteres")
        private String state;
        
        @Pattern(regexp = "^[0-9]{5}$", message = "El código postal debe tener 5 dígitos")
        private String zipCode;
        
        @Builder.Default
        private String country = "Perú";
    }

    /**
     * Método para obtener nombre completo
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Validación personalizada para username único
     */
    public boolean isValidUsername() {
        if (username == null || username.trim().isEmpty()) {
            return true; // Username es opcional
        }
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }
}