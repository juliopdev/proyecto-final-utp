package com.utp.security;

import com.utp.proyectofinal.models.entities.User;
import com.utp.proyectofinal.repositories.postgresql.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio personalizado para cargar detalles de usuario
 * 
 * PATRONES IMPLEMENTADOS:
 * - ADAPTER: Adapta la entidad User a UserDetails de Spring Security
 * - PROXY: Actúa como proxy entre Spring Security y el repositorio de usuarios
 * - TEMPLATE METHOD: Implementa el template definido por UserDetailsService
 * 
 * @author Julio Pariona
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * PATRÓN TEMPLATE METHOD: Implementación del método template
     * PATRÓN ADAPTER: Convierte User entity a UserDetails
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Intentando cargar usuario por email: {}", email);
        
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado con email: {}", email);
                    return new UsernameNotFoundException("Usuario no encontrado con email: " + email);
                });

        if (!user.isEnabled()) {
            log.warn("Usuario deshabilitado con email: {}", email);
            throw new UsernameNotFoundException("Usuario deshabilitado: " + email);
        }

        log.debug("Usuario cargado exitosamente: {}", email);
        return buildUserDetails(user);
    }

    /**
     * PATRÓN ADAPTER: Convierte entidad User a UserDetails
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .accountExpired(false)
                .accountLocked(user.isLocked())
                .credentialsExpired(false)
                .authorities(getAuthorities(user))
                .build();
    }

    /**
     * Extrae las autoridades/roles del usuario
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> {
                    System.out.println("CustomUserDetailsService.java - 81: " + role.getName());
                    return new SimpleGrantedAuthority("ROLE_" + role.getName());
                })
                // .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Método adicional para obtener usuario por ID (usado por JWT)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        log.debug("Cargando usuario por ID: {}", userId);
        
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado con ID: {}", userId);
                    return new UsernameNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        return buildUserDetails(user);
    }
}