// package com.utp.proyectofinal.controllers.api;

// import com.utp.proyectofinal.models.dto.AuthResponseDTO;
// import com.utp.proyectofinal.models.dto.LoginRequestDTO;
// import com.utp.proyectofinal.models.dto.RegisterRequestDTO;
// import com.utp.proyectofinal.services.interfaces.AuthService;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
// import org.springframework.validation.BindingResult;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
// @RequiredArgsConstructor
// public class AuthApiController {

//     private final AuthService authService;

//     @PostMapping("/login")
//     public ResponseEntity<AuthResponseDTO> login(
//             @Valid @RequestBody LoginRequestDTO loginRequest,
//             BindingResult bindingResult
//     ) {
//         if (bindingResult.hasErrors()) {
//             return ResponseEntity.badRequest()
//                     .body(new AuthResponseDTO(false, "Datos de login inválidos", null));
//         }

//         try {
//             String token = authService.authenticateCliente(loginRequest);
//             return ResponseEntity.ok(
//                     new AuthResponseDTO(true, "Login exitoso", token)
//             );
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                     .body(new AuthResponseDTO(false, "Email o contraseña incorrectos", null));
//         }
//     }

//     @PostMapping("/register")
//     public ResponseEntity<AuthResponseDTO> register(
//             @Valid @RequestBody RegisterRequestDTO registerRequest,
//             BindingResult bindingResult
//     ) {
//         if (bindingResult.hasErrors()) {
//             return ResponseEntity.badRequest()
//                     .body(new AuthResponseDTO(false, "Datos de registro inválidos", null));
//         }

//         try {
//             authService.registerCliente(registerRequest);
//             return ResponseEntity.status(HttpStatus.CREATED)
//                     .body(new AuthResponseDTO(true, "Registro exitoso. Verifica tu email.", null));
//         } catch (Exception e) {
//             return ResponseEntity.badRequest()
//                     .body(new AuthResponseDTO(false, e.getMessage(), null));
//         }
//     }

//     @PostMapping("/logout")
//     public ResponseEntity<AuthResponseDTO> logout(
//             HttpServletRequest request,
//             HttpServletResponse response
//     ) {
//         try {
//             Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//             if (auth != null) {
//                 new SecurityContextLogoutHandler().logout(request, response, auth);
//             }
//             return ResponseEntity.ok(
//                     new AuthResponseDTO(true, "Logout exitoso", null)
//             );
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(new AuthResponseDTO(false, "Error al cerrar sesión", null));
//         }
//     }

//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<AuthResponseDTO> handleException(Exception e) {
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                 .body(new AuthResponseDTO(false, "Error interno del servidor", null));
//     }
// }