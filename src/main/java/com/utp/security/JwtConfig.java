// package com.utp.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;

// import javax.crypto.SecretKey;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;

// @Component
// @Slf4j
// public class JwtConfig {

//     @Value("${jwt.secret:mySecretKey}")
//     private String secretKey;

//     @Value("${jwt.expiration:86400000}") // 24 horas por defecto
//     private long jwtExpiration;

//     private SecretKey getSigningKey() {
//         return Keys.hmacShaKeyFor(secretKey.getBytes());
//     }

//     public String generateToken(UserDetails userDetails) {
//         return generateToken(new HashMap<>(), userDetails);
//     }

//     public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//         return Jwts.builder()
//                 .setClaims(extraClaims)
//                 .setSubject(userDetails.getUsername())
//                 .setIssuedAt(new Date(System.currentTimeMillis()))
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
//                 .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                 .compact();
//     }

//     public boolean isTokenValid(String token, UserDetails userDetails) {
//         try {
//             final String username = extractUsername(token);
//             return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
//         } catch (Exception e) {
//             log.error("Token inválido: {}", e.getMessage());
//             return false;
//         }
//     }

//     public String extractUsername(String token) {
//         return extractAllClaims(token).getSubject();
//     }

//     private Claims extractAllClaims(String token) {
//         return Jwts.parserBuilder()
//                 .setSigningKey(getSigningKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     private boolean isTokenExpired(String token) {
//         return extractAllClaims(token).getExpiration().before(new Date());
//     }
// }