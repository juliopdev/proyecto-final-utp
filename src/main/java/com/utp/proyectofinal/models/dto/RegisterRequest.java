package com.utp.proyectofinal.models.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para Register Request
 * 
 * PATRONES IMPLEMENTADOS:
 * - DTO: Data Transfer Object para transferencia de datos
 * - BUILDER: Patrón Builder con Lombok
 * - VALIDATION: Validación completa con Bean Validation
 * 
 * @author Julio Pariona
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "La contraseña debe contener al menos: 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial"
    )
    private String password;

    @NotBlank(message = "Confirmar contraseña es obligatorio")
    private String confirmPassword;

    @Pattern(
        regexp = "^\\+?[0-9]{8,15}$", 
        message = "El teléfono debe tener entre 8 y 15 dígitos",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    private String phone;

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate birthDate;

    @Size(max = 222, message = "La dirección no puede exceder 222 caracteres")
    private String address;

    @NotNull(message = "Debe aceptar los términos y condiciones")
    @AssertTrue(message = "Debe aceptar los términos y condiciones")
    private Boolean acceptTerms;

    @Builder.Default
    private Boolean acceptMarketing = false;

    /**
     * Validación personalizada para confirmar contraseña
     */
    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordMatching() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Validación de edad mínima
     */
    @AssertTrue(message = "Debe ser mayor de 13 años")
    public boolean isValidAge() {
        if (birthDate == null) {
            return true; // Permitir null, será validado por @Past
        }
        return birthDate.isBefore(LocalDate.now().minusYears(13));
    }

    /**
     * Método para obtener nombre completo
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * toString sin passwords para logging seguro
     */
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", birthDate=" + birthDate +
                ", acceptTerms=" + acceptTerms +
                ", acceptMarketing=" + acceptMarketing +
                '}';
    }
}