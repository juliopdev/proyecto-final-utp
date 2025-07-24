package com.utp.proyectofinal.services.impl;

import com.utp.proyectofinal.models.dto.ClienteDTO;
import com.utp.proyectofinal.models.dto.LoginRequestDTO;
import com.utp.proyectofinal.models.dto.RegisterRequestDTO;
import com.utp.proyectofinal.models.dto.RolDTO;
import com.utp.proyectofinal.models.entities.Cliente;
import com.utp.proyectofinal.models.entities.Rol;
import com.utp.proyectofinal.repositories.postgresql.RolRepository;
import com.utp.proyectofinal.repositories.postgresql.ClienteRepository;
import com.utp.proyectofinal.services.interfaces.AuthService;
import com.utp.proyectofinal.services.interfaces.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public Cliente registerCliente(RegisterRequestDTO registerRequest) {
        
        // Verificar si el cliente ya existe
        if (existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Validar que las contraseñas coincidan
        validatePasswordMatch(registerRequest.getPassword(), registerRequest.getConfirmPassword());
        
        // Buscar rol de cliente (asumiendo que existe un rol "CLIENTE")
        Rol rolCliente = rolRepository.findByNombreRol("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));
        
        // Crear nueva entidad Cliente directamente
        Cliente cliente = new Cliente();
        cliente.setNombre(registerRequest.getName());
        cliente.setEmail(registerRequest.getEmail());
        cliente.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        cliente.setRol(rolCliente);
        
        // Agregar campos opcionales si están presentes
        if (registerRequest.getTelefono() != null && !registerRequest.getTelefono().trim().isEmpty()) {
            cliente.setTelefono(registerRequest.getTelefono());
        }
        if (registerRequest.getDireccion() != null && !registerRequest.getDireccion().trim().isEmpty()) {
            cliente.setDireccion(registerRequest.getDireccion());
        }
        if (registerRequest.getDni() != null && !registerRequest.getDni().trim().isEmpty()) {
            cliente.setDni(registerRequest.getDni());
        }
        if (registerRequest.getRuc() != null && !registerRequest.getRuc().trim().isEmpty()) {
            cliente.setRuc(registerRequest.getRuc());
        }
        
        Cliente savedCliente = clienteRepository.save(cliente);
        log.info("Cliente registrado exitosamente: {}", savedCliente.getEmail());
        
        return savedCliente;
    }

    @Override
    public String authenticateCliente(LoginRequestDTO loginRequest) {
        log.info("Iniciando autenticación para cliente: {}", loginRequest.getEmail());
        
        try {
            // Autenticar cliente
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            
            // Buscar cliente completo
            Cliente cliente = findClienteByEmail(loginRequest.getEmail());
            if (cliente == null) {
                throw new RuntimeException("Cliente no encontrado");
            }
            
            // Generar token JWT
            String token = jwtService.generateToken(cliente);
            
            log.info("Autenticación exitosa para cliente: {}", loginRequest.getEmail());
            return token;
            
        } catch (AuthenticationException e) {
            log.warn("Fallo en autenticación para cliente: {}", loginRequest.getEmail());
            throw new RuntimeException("Email o contraseña incorrectos");
        }
    }

    @Override
    public Cliente findClienteByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return clienteRepository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return clienteRepository.existsByEmail(email);
    }

    @Override
    public void validatePasswordMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            throw new RuntimeException("Las contraseñas no pueden ser nulas");
        }
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }
    }
}