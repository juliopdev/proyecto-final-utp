package com.utp.proyectofinal.services.impl;

import com.utp.proyectofinal.models.entities.Cliente;
import com.utp.proyectofinal.services.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 horas en milisegundos
    private long jwtExpiration;

    @Override
    public String generateToken(Cliente cliente) {
        return generateToken(new HashMap<>(), cliente);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateTokenFromUserDetails(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, Cliente cliente) {
        return buildTokenFromCliente(extraClaims, cliente, jwtExpiration);
    }

    private String buildTokenFromCliente(
            Map<String, Object> extraClaims,
            Cliente cliente,
            long expiration
    ) {
        log.debug("Generando token para cliente: {}", cliente.getEmail());
        
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(cliente.getEmail())
                .claim("clienteId", cliente.getId().toString())
                .claim("nombre", cliente.getNombre())
                .claim("rol", cliente.getRol() != null ? cliente.getRol().getNombreRol() : "USER")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateTokenFromUserDetails(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.debug("Generando token para UserDetails: {}", userDetails.getUsername());
        
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractClienteId(String token) {
        return extractClaim(token, claims -> claims.get("clienteId", String.class));
    }

    public String extractNombre(String token) {
        return extractClaim(token, claims -> claims.get("nombre", String.class));
    }

    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error extrayendo claims del token: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isTokenValid(String token, Cliente cliente) {
        try {
            final String username = extractUsername(token);
            return username.equals(cliente.getEmail()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey getSignInKey() {
        // Si la clave está en Base64, decodificarla; si no, usar directamente los bytes
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secretKey);
        } catch (IllegalArgumentException e) {
            // Si no es Base64 válido, usar los bytes directos
            keyBytes = secretKey.getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Método de utilidad para convertir Cliente a UserDetails
    public UserDetails clienteToUserDetails(Cliente cliente) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (cliente.getRol() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + cliente.getRol().getNombreRol()));
        }

        return User.builder()
                .username(cliente.getEmail())
                .password(cliente.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}