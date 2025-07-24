package com.utp.proyectofinal.services.impl;

import com.utp.proyectofinal.models.entities.Cliente;
import com.utp.proyectofinal.repositories.postgresql.ClienteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service("authUserDetailsService") 
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Cargando cliente por email: {}", email);
        
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado: {}", email);
                    return new UsernameNotFoundException("Cliente no encontrado: " + email);
                });

        log.debug("Cliente encontrado: {}", cliente.getEmail());
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(cliente.getEmail())
                .password(cliente.getPasswordHash())
                .authorities(Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + cliente.getRol().getNombreRol())
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}