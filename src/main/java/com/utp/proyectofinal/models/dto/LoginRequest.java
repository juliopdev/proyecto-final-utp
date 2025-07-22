package com.utp.proyectofinal.models.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para Login Request
 * 
 * PATRONES IMPLEMENTADOS:
 * - DTO: Data Transfer Object para transferencia de datos
 * - BUILDER: Patrón Builder con Lombok
 * - VALIDATION: Validación con Bean Validation
 * 
 * @author Julio Pariona
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;

    @Builder.Default
    private Boolean rememberMe = false;

    /**
     * toString sin password para logging seguro
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
}