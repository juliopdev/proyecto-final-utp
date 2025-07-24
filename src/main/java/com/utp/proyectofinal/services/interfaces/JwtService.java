package com.utp.proyectofinal.services.interfaces;

import com.utp.proyectofinal.models.entities.Cliente;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.function.Function;

public interface JwtService {
    String generateToken(Cliente cliente);
    String generateToken(UserDetails userDetails);
    String extractUsername(String token);
    Date extractExpiration(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    boolean isTokenValid(String token, Cliente cliente);
    boolean isTokenValid(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
}