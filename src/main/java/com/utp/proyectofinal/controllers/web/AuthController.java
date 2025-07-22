package com.utp.proyectofinal.controllers.web;

import com.utp.proyectofinal.models.dto.LoginRequest;
import com.utp.proyectofinal.models.dto.RegisterRequest;
import com.utp.proyectofinal.services.interfaces.AuthService;
import com.utp.proyectofinal.services.interfaces.LogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de Autenticación SSR
 * 
 * PATRONES IMPLEMENTADOS:
 * - MVC: Patrón Model-View-Controller para arquitectura web
 * - COMMAND: Cada método representa un comando de autenticación
 * - TEMPLATE METHOD: Estructura común para manejo de autenticación
 * - STRATEGY: Diferentes estrategias de autenticación (login, registro, logout)
 * - OBSERVER: Registra eventos de autenticación en logs
 * 
 * @author Julio Pariona
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final LogService logService;

    /**
     * PATRÓN TEMPLATE METHOD: Template para mostrar formulario de login
     */
    @GetMapping("/login")
    public String showLoginForm(
            Model model,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        log.debug("Mostrando formulario de login");

        if (error != null) {
            model.addAttribute("errorMessage", "Email o contraseña incorrectos");
            log.debug("Error en login detectado");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Has cerrado sesión exitosamente");
            log.debug("Logout exitoso detectado");
        }

        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("redirectUrl", redirectUrl);
        
        return "auth/login";
    }

    /**
     * PATRÓN COMMAND: Comando para procesar login
     * Spring Security maneja la autenticación real, este método es para casos especiales
     */
    @PostMapping("/login")
    public String processLogin(
            @Valid @ModelAttribute LoginRequest loginRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Procesando intento de login para: {}", loginRequest.getEmail());

        if (bindingResult.hasErrors()) {
            log.warn("Errores de validación en login: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("errorMessage", "Datos de login inválidos");
            return "redirect:/auth/login?error=validation";
        }

        // Spring Security maneja la autenticación
        // Este método se ejecuta solo si hay lógica adicional necesaria
        
        return "redirect:/dashboard";
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template para mostrar formulario de registro
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        log.debug("Mostrando formulario de registro");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    /**
     * PATRÓN COMMAND: Comando para procesar registro
     */
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Procesando registro para: {}", registerRequest.getEmail());

        if (bindingResult.hasErrors()) {
            log.warn("Errores de validación en registro: {}", bindingResult.getAllErrors());
            return "auth/register";
        }

        try {
            // PATRÓN STRATEGY: Estrategia de registro de usuario
            authService.registerUser(registerRequest, getClientIP(request));
            
            // PATRÓN OBSERVER: Registrar evento de registro
            logService.logUserRegistration(registerRequest.getEmail(), getClientIP(request));
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registro exitoso. Por favor verifica tu email antes de iniciar sesión.");
            
            log.info("Usuario registrado exitosamente: {}", registerRequest.getEmail());
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            log.error("Error en registro de usuario: {}", e.getMessage());
            
            // PATRÓN OBSERVER: Registrar error de registro
            logService.logRegistrationError(registerRequest.getEmail(), e.getMessage(), getClientIP(request));
            
            bindingResult.rejectValue("email", "registration.failed", e.getMessage());
            return "auth/register";
        }
    }

    /**
     * PATRÓN COMMAND: Comando para logout personalizado
     */
    @GetMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            String userEmail = auth.getName();
            log.debug("Procesando logout para: {}", userEmail);
            
            // PATRÓN OBSERVER: Registrar evento de logout
            logService.logUserLogout(userEmail, getClientIP(request));
            
            // Limpiar contexto de seguridad
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Has cerrado sesión exitosamente");
        return "redirect:/home";
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template para verificación de email
     */
    @GetMapping("/verify")
    public String verifyEmail(
            @RequestParam("token") String token,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Procesando verificación de email con token: {}", token.substring(0, 10) + "...");
        
        try {
            // PATRÓN STRATEGY: Estrategia de verificación
            authService.verifyEmail(token);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Email verificado exitosamente. Ya puedes iniciar sesión.");
            
            log.info("Email verificado exitosamente para token: {}", token.substring(0, 10) + "...");
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            log.error("Error en verificación de email: {}", e.getMessage());
            
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Token de verificación inválido o expirado.");
            
            return "redirect:/auth/login?error=verification";
        }
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template para solicitar reset de password
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        log.debug("Mostrando formulario de recuperación de contraseña");
        return "auth/forgot-password";
    }

    /**
     * PATRÓN COMMAND: Comando para procesar solicitud de reset
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Procesando solicitud de recuperación para: {}", email);
        
        try {
            // PATRÓN STRATEGY: Estrategia de recuperación de contraseña
            authService.initiatePasswordReset(email, getClientIP(request));
            
            // PATRÓN OBSERVER: Registrar solicitud de recuperación
            logService.logPasswordResetRequest(email, getClientIP(request));
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Si el email existe, recibirás instrucciones para recuperar tu contraseña.");
            
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            log.error("Error en recuperación de contraseña: {}", e.getMessage());
            
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al procesar la solicitud. Inténtalo nuevamente.");
            
            return "redirect:/auth/forgot-password";
        }
    }

    /**
     * PATRÓN TEMPLATE METHOD: Template para reset de password
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam("token") String token,
            Model model
    ) {
        log.debug("Mostrando formulario de reset de contraseña");
        
        // Validar token antes de mostrar formulario
        if (!authService.isValidResetToken(token)) {
            log.warn("Token de reset inválido: {}", token.substring(0, 10) + "...");
            model.addAttribute("errorMessage", "Token inválido o expirado");
            return "auth/login";
        }
        
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    /**
     * PATRÓN COMMAND: Comando para procesar reset de password
     */
    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Procesando reset de contraseña");
        
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden");
            return "redirect:/auth/reset-password?token=" + token;
        }
        
        try {
            // PATRÓN STRATEGY: Estrategia de reset de contraseña
            String userEmail = authService.resetPassword(token, password);
            
            // PATRÓN OBSERVER: Registrar reset exitoso
            logService.logPasswordReset(userEmail, getClientIP(request));
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Contraseña actualizada exitosamente. Inicia sesión con tu nueva contraseña.");
            
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            log.error("Error en reset de contraseña: {}", e.getMessage());
            
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }

    /**
     * Método utilitario para obtener IP del cliente
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    /**
     * PATRÓN STRATEGY: Manejar diferentes tipos de errores de autenticación
     */
    @ExceptionHandler(Exception.class)
    public String handleAuthException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Error en autenticación: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Error interno del servidor");
        return "redirect:/auth/login?error=server";
    }
}