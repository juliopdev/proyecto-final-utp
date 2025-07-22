package com.utp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry Point personalizado para manejar errores de autenticación
 * 
 * PATRONES IMPLEMENTADOS:
 * - STRATEGY: Diferentes estrategias de respuesta según el tipo de request (API vs SSR)
 * - TEMPLATE METHOD: Implementa el template AuthenticationEntryPoint
 * - ADAPTER: Adapta diferentes tipos de errores a respuestas HTTP
 * 
 * @author Julio Pariona
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * PATRÓN TEMPLATE METHOD: Implementación del método template
     * PATRÓN STRATEGY: Diferentes estrategias según el tipo de request
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        String requestURI = request.getRequestURI();
        log.warn("Acceso no autorizado a: {} - Error: {}", requestURI, authException.getMessage());

        // PATRÓN STRATEGY: Estrategia para APIs
        if (isApiRequest(request)) {
            handleApiAuthenticationError(response, authException, requestURI);
        } else {
            // PATRÓN STRATEGY: Estrategia para SSR
            handleSSRAuthenticationError(response, requestURI);
        }
    }

    /**
     * PATRÓN ADAPTER: Adapta error de autenticación a respuesta JSON para APIs
     */
    private void handleApiAuthenticationError(
            HttpServletResponse response,
            AuthenticationException authException,
            String requestURI
    ) throws IOException {
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Token de acceso inválido o expirado");
        errorResponse.put("path", requestURI);
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        
        log.debug("Respuesta de error API enviada para: {}", requestURI);
    }

    /**
     * PATRÓN ADAPTER: Adapta error de autenticación a redirección para SSR
     */
    private void handleSSRAuthenticationError(HttpServletResponse response, String requestURI) throws IOException {
        // Para requests SSR, redirigir a login
        String loginUrl = "/login";
        
        // Preservar la URL original como parámetro para redireccionar después del login
        if (!requestURI.equals("/") && !requestURI.startsWith("/login")) {
            loginUrl += "?redirect=" + requestURI;
        }
        
        response.sendRedirect(loginUrl);
        log.debug("Redirección SSR a login desde: {}", requestURI);
    }

    /**
     * PATRÓN STRATEGY: Estrategia para identificar requests de API
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getHeader("Content-Type");
        
        // Es request de API si:
        // 1. La URI comienza con /api/
        // 2. Accept header contiene application/json
        // 3. Content-Type es application/json
        // 4. Hay header Authorization con Bearer
        
        boolean isApiPath = requestURI.startsWith("/api/");
        boolean acceptsJson = acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE);
        boolean contentTypeJson = contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
        boolean hasBearerToken = request.getHeader("Authorization") != null && 
                                request.getHeader("Authorization").startsWith("Bearer ");
        
        return isApiPath || acceptsJson || contentTypeJson || hasBearerToken;
    }
}