package com.utp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para procesar tokens JWT en cada request
 * 
 * PATRONES IMPLEMENTADOS:
 * - PROXY: Actúa como proxy entre el request y el sistema de autenticación
 * - TEMPLATE METHOD: Extiende OncePerRequestFilter implementando doFilterInternal
 * - STRATEGY: Diferentes estrategias de autenticación según el tipo de request
 * - COMMAND: Encapsula la lógica de autenticación en comandos ejecutables
 * 
 * @author Julio Pariona
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtConfig jwtConfig;

    /**
     * PATRÓN TEMPLATE METHOD: Implementación del método template
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // PATRÓN STRATEGY: Diferentes estrategias según el endpoint
        String requestPath = request.getRequestURI();
        
        // Para endpoints SSR, permitir autenticación por sesión
        if (isSSREndpoint(requestPath)) {
            log.debug("Endpoint SSR detectado: {}, permitiendo autenticación por sesión", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Para APIs, procesar JWT
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Verificar si hay token JWT
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No se encontró token JWT en el header Authorization para: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // PATRÓN COMMAND: Ejecutar comando de procesamiento de JWT
        try {
            jwt = authHeader.substring(7);
            userEmail = jwtConfig.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(request, jwt, userEmail);
            }
        } catch (Exception e) {
            log.error("Error procesando token JWT: {}", e.getMessage());
            // Continuar sin autenticación, dejar que Spring Security maneje el acceso denegado
        }

        filterChain.doFilter(request, response);
    }

    /**
     * PATRÓN COMMAND: Comando para autenticar usuario
     */
    private void authenticateUser(HttpServletRequest request, String jwt, String userEmail) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            
            if (jwtConfig.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("Usuario autenticado exitosamente via JWT: {}", userEmail);
            } else {
                log.warn("Token JWT inválido para usuario: {}", userEmail);
            }
        } catch (Exception e) {
            log.error("Error autenticando usuario {}: {}", userEmail, e.getMessage());
        }
    }

    /**
     * PATRÓN STRATEGY: Estrategia para identificar endpoints SSR
     */
    private boolean isSSREndpoint(String requestPath) {
        return requestPath.startsWith("/home") ||
               requestPath.startsWith("/products") ||
               requestPath.startsWith("/cart") ||
               requestPath.startsWith("/checkout") ||
               requestPath.startsWith("/dashboard") ||
               requestPath.startsWith("/login") ||
               requestPath.startsWith("/register") ||
               requestPath.equals("/") ||
               requestPath.startsWith("/css") ||
               requestPath.startsWith("/js") ||
               requestPath.startsWith("/img") ||
               requestPath.startsWith("/assets") ||
               requestPath.equals("/favicon.ico");
    }

    /**
     * No filtrar requests a recursos estáticos
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/img/") || 
               path.startsWith("/assets/") ||
               path.equals("/favicon.ico");
    }
}