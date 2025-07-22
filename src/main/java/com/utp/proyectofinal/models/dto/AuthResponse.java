package com.utp.proyectofinal.models.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para respuestas de autenticación
 * 
 * PATRONES IMPLEMENTADOS:
 * - DTO: Data Transfer Object para respuestas
 * - BUILDER: Patrón Builder con Lombok
 * 
 * @author Julio Pariona
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
    
    // Información del usuario
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private Boolean verified;
    
    // URLs de redirección
    private String redirectUrl;
    
    /**
     * PATRÓN FACTORY: Factory method para respuesta exitosa
     */
    public static AuthResponse success(String token, Long userId, String email, String fullName, String role) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 horas
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .role(role)
                .build();
    }

    /**
     * PATRÓN FACTORY: Factory method para respuesta con redirección
     */
    public static AuthResponse successWithRedirect(String token, Long userId, String email, 
                                                  String fullName, String role, String redirectUrl) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .role(role)
                .redirectUrl(redirectUrl)
                .build();
    }
}