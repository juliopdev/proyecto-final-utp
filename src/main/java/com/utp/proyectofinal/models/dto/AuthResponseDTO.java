package com.utp.proyectofinal.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private boolean success;
    private String message;
    private String token;
}