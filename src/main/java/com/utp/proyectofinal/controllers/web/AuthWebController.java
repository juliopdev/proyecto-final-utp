package com.utp.proyectofinal.controllers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthWebController {

    @GetMapping("/login")
    public String showLoginForm(
            Model model,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout
    ) {
        if (error != null) {
            model.addAttribute("errorMessage", "Email o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Has cerrado sesión exitosamente");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(
            Model model,
            @RequestParam(value = "error", required = false) String error
    ) {
        if (error != null) {
            switch (error) {
                case "email-exists":
                    model.addAttribute("errorMessage", "El email ya está registrado");
                    break;
                case "password-mismatch":
                    model.addAttribute("errorMessage", "Las contraseñas no coinciden");
                    break;
                case "invalid-email":
                    model.addAttribute("errorMessage", "Email inválido");
                    break;
                case "invalid-password":
                    model.addAttribute("errorMessage", "La contraseña debe tener al menos 8 caracteres");
                    break;
                default:
                    model.addAttribute("errorMessage", "Error en el registro");
            }
        }
        return "auth/register";
    }

    @GetMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Has cerrado sesión exitosamente");
        return "redirect:/home";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Error interno del servidor");
        return "redirect:/";
    }
}