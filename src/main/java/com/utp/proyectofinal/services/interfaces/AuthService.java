package com.utp.proyectofinal.services.interfaces;

import com.utp.proyectofinal.models.dto.LoginRequestDTO;
import com.utp.proyectofinal.models.dto.RegisterRequestDTO;
import com.utp.proyectofinal.models.entities.Cliente;

public interface AuthService {
    
    /**
     * Registra un nuevo cliente en el sistema
     * @param registerRequest datos del cliente a registrar
     * @return Cliente registrado
     * @throws RuntimeException si el email ya existe o los datos son inválidos
     */
    Cliente registerCliente(RegisterRequestDTO registerRequest);
    
    /**
     * Autentica un cliente y genera un token JWT
     * @param loginRequest credenciales del cliente
     * @return Token JWT
     * @throws RuntimeException si las credenciales son incorrectas
     */
    String authenticateCliente(LoginRequestDTO loginRequest);
    
    /**
     * Busca un cliente por email
     * @param email email del cliente
     * @return Cliente encontrado o null
     */
    Cliente findClienteByEmail(String email);
    
    /**
     * Verifica si un email ya está registrado
     * @param email email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
    
    /**
     * Valida que las contraseñas coincidan
     * @param password contraseña
     * @param confirmPassword confirmación de contraseña
     * @throws RuntimeException si no coinciden
     */
    void validatePasswordMatch(String password, String confirmPassword);
}