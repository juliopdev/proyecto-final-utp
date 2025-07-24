// package com.utp.security;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class JwtRequestFilter extends OncePerRequestFilter {

//     private final CustomUserDetailsService userDetailsService;
//     private final JwtConfig jwtConfig;

//     @Override
//     protected void doFilterInternal(
//             HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain filterChain
//     ) throws ServletException, IOException {

//         final String authorizationHeader = request.getHeader("Authorization");

//         String username = null;
//         String jwt = null;

//         // Extraer JWT del header Authorization
//         if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//             jwt = authorizationHeader.substring(7);
//             try {
//                 username = jwtConfig.extractUsername(jwt);
//             } catch (Exception e) {
//                 log.error("Error extrayendo username del token: {}", e.getMessage());
//             }
//         }

//         // Validar token y establecer autenticación
//         if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//             UserDetails userDetails = userDetailsService.loadUserByUsername(username);

//             if (jwtConfig.isTokenValid(jwt, userDetails)) {
//                 UsernamePasswordAuthenticationToken authToken = 
//                     new UsernamePasswordAuthenticationToken(
//                         userDetails, 
//                         null, 
//                         userDetails.getAuthorities()
//                     );
                
//                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                 SecurityContextHolder.getContext().setAuthentication(authToken);
//             }
//         }

//         filterChain.doFilter(request, response);
//     }
// }