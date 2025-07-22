package com.utp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Configuración y utilidades para JWT
 * 
 * PATRONES IMPLEMENTADOS:
 * - SINGLETON: Component único para manejo de JWT
 * - STRATEGY: Diferentes estrategias para validación y generación de tokens
 * - TEMPLATE METHOD: Métodos template para extraer claims y validar tokens
 * - FACTORY: Factory methods para crear tokens
 * 
 * @author Julio Pariona
 */
@Component
@Slf4j
public class JwtConfig {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * PATRÓN FACTORY: Factory method para generar tokens
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * PATRÓN FACTORY: Factory method con claims adicionales
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template para construir tokens
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        log.debug("Generando token JWT para usuario: {}", userDetails.getUsername());
        
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * PATRÓN STRATEGY: Estrategia para validar tokens
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
            
            if (isValid) {
                log.debug("Token válido para usuario: {}", username);
            } else {
                log.warn("Token inválido para usuario: {}", username);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template para extraer username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template para extraer fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template genérico para extraer claims
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error extrayendo claims del token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * PATRÓN STRATEGY: Estrategia para verificar expiración
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean expired = expiration.before(new Date());
        
        if (expired) {
            log.debug("Token expirado en: {}", expiration);
        }
        
        return expired;
    }

    /**
     * Obtiene el tiempo de expiración configurado
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
}