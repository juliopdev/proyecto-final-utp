// package com.utp.security;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.MediaType;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.AuthenticationEntryPoint;
// import org.springframework.stereotype.Component;

// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;

// @Component
// @Slf4j
// public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

//     private final ObjectMapper objectMapper = new ObjectMapper();

//     @Override
//     public void commence(
//             HttpServletRequest request,
//             HttpServletResponse response,
//             AuthenticationException authException
//     ) throws IOException {

//         String requestURI = request.getRequestURI();
//         log.warn("Acceso no autorizado a: {}", requestURI);

//         // Si es una petición a la API, devolver JSON
//         if (isApiRequest(request)) {
//             handleApiError(response, requestURI);
//         } else {
//             // Si es una petición web, redirigir al login
//             response.sendRedirect("/login");
//         }
//     }

//     private void handleApiError(HttpServletResponse response, String requestURI) throws IOException {
//         response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

//         Map<String, Object> errorResponse = new HashMap<>();
//         errorResponse.put("error", "No autorizado");
//         errorResponse.put("message", "Se requiere autenticación");
//         errorResponse.put("path", requestURI);
//         errorResponse.put("status", 401);

//         response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
//     }

//     private boolean isApiRequest(HttpServletRequest request) {
//         String requestURI = request.getRequestURI();
//         String acceptHeader = request.getHeader("Accept");
        
//         return requestURI.startsWith("/api/") || 
//                (acceptHeader != null && acceptHeader.contains("application/json"));
//     }
// }