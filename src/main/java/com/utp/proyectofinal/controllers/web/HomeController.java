package com.utp.proyectofinal.controllers.web;

import com.utp.proyectofinal.models.entities.Product;
import com.utp.proyectofinal.services.interfaces.ProductService;
import com.utp.proyectofinal.services.interfaces.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador principal para páginas públicas
 * 
 * PATRONES IMPLEMENTADOS:
 * - MVC: Patrón Model-View-Controller para arquitectura web
 * - COMMAND: Cada método representa un comando de navegación
 * - TEMPLATE METHOD: Estructura común para renderizado de vistas
 * - OBSERVER: Registra eventos de navegación en logs
 * - FACADE: Fachada que coordina servicios para vistas
 * 
 * @author Julio Pariona
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ProductService productService;
    private final LogService logService;

    /**
     * PATRÓN TEMPLATE METHOD: Template para página principal
     */
    @GetMapping({"/", "/home"})
    public String home(Model model, HttpServletRequest request) {
        log.debug("Accediendo a página principal");

        try {
            // PATRÓN FACADE: Obtener datos para la vista principal
            List<Product> featuredProducts = productService.getFeaturedProducts(8);
            List<Product> recentProducts = productService.getRecentProducts(6);
            
            // PATRÓN STRATEGY: Diferentes contenidos según usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                    !auth.getName().equals("anonymousUser");

            // Preparar modelo para la vista
            model.addAttribute("featuredProducts", featuredProducts);
            model.addAttribute("recentProducts", recentProducts);
            model.addAttribute("isAuthenticated", isAuthenticated);
            model.addAttribute("pageTitle", "Bienvenido - Proyecto Final UTP");
            model.addAttribute("pageDescription", "Descubre nuestros productos destacados");

            if (isAuthenticated) {
                model.addAttribute("userName", auth.getName());
                model.addAttribute("welcomeMessage", "¡Bienvenido de vuelta!");
            } else {
                model.addAttribute("welcomeMessage", "¡Bienvenido a nuestra tienda!");
            }

            // PATRÓN OBSERVER: Registrar visita a home
            String userEmail = isAuthenticated ? auth.getName() : "anonymous";
            logService.logPageView("HOME", userEmail, getClientIP(request));

            log.debug("Página principal cargada exitosamente para: {}", userEmail);
            return "home/index";

        } catch (Exception e) {
            log.error("Error cargando página principal: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error cargando la página principal");
            return "error/500";
        }
    }

    /**
     * PATRÓN COMMAND: Comando para página "Acerca de"
     */
    @GetMapping("/about")
    public String about(Model model, HttpServletRequest request) {
        log.debug("Accediendo a página 'Acerca de'");

        model.addAttribute("pageTitle", "Acerca de Nosotros - Proyecto Final UTP");
        model.addAttribute("pageDescription", "Conoce más sobre nuestro proyecto");
        
        // PATRÓN OBSERVER: Registrar visita
        String userEmail = getCurrentUserEmail();
        logService.logPageView("ABOUT", userEmail, getClientIP(request));

        return "home/about";
    }

    /**
     * PATRÓN COMMAND: Comando para página de contacto
     */
    @GetMapping("/contact")
    public String contact(Model model, HttpServletRequest request) {
        log.debug("Accediendo a página de contacto");

        model.addAttribute("pageTitle", "Contacto - Proyecto Final UTP");
        model.addAttribute("pageDescription", "Ponte en contacto con nosotros");
        
        // PATRÓN OBSERVER: Registrar visita
        String userEmail = getCurrentUserEmail();
        logService.logPageView("CONTACT", userEmail, getClientIP(request));

        return "home/contact";
    }

    /**
     * PATRÓN COMMAND: Procesar formulario de contacto
     */
    @PostMapping("/contact")
    public String processContact(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            Model model,
            HttpServletRequest request) {
        
        log.debug("Procesando formulario de contacto desde: {}", email);

        try {
            // PATRÓN COMMAND: Procesar mensaje de contacto
            // Aquí podrías integrar con EmailService para enviar el mensaje
            
            // PATRÓN OBSERVER: Registrar envío de contacto
            logService.logContactFormSubmission(name, email, subject, getClientIP(request));

            model.addAttribute("successMessage", "Tu mensaje ha sido enviado exitosamente. Te contactaremos pronto.");
            model.addAttribute("pageTitle", "Contacto - Proyecto Final UTP");
            
            log.info("Formulario de contacto procesado exitosamente para: {} ({})", name, email);
            return "home/contact";

        } catch (Exception e) {
            log.error("Error procesando formulario de contacto: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error enviando tu mensaje. Por favor intenta nuevamente.");
            model.addAttribute("pageTitle", "Contacto - Proyecto Final UTP");
            return "home/contact";
        }
    }

    /**
     * PATRÓN COMMAND: Comando para términos y condiciones
     */
    @GetMapping("/terms")
    public String terms(Model model, HttpServletRequest request) {
        log.debug("Accediendo a términos y condiciones");

        model.addAttribute("pageTitle", "Términos y Condiciones - Proyecto Final UTP");
        
        // PATRÓN OBSERVER: Registrar visita
        String userEmail = getCurrentUserEmail();
        logService.logPageView("TERMS", userEmail, getClientIP(request));

        return "home/terms";
    }

    /**
     * PATRÓN COMMAND: Comando para política de privacidad
     */
    @GetMapping("/privacy")
    public String privacy(Model model, HttpServletRequest request) {
        log.debug("Accediendo a política de privacidad");

        model.addAttribute("pageTitle", "Política de Privacidad - Proyecto Final UTP");
        
        // PATRÓN OBSERVER: Registrar visita
        String userEmail = getCurrentUserEmail();
        logService.logPageView("PRIVACY", userEmail, getClientIP(request));

        return "home/privacy";
    }

    /**
     * PATRÓN COMMAND: Comando para página de búsqueda
     */
    @GetMapping("/search")
    public String search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model,
            HttpServletRequest request) {
        
        log.debug("Realizando búsqueda: query='{}', category='{}'", query, category);

        try {
            if (query != null && !query.trim().isEmpty()) {
                // PATRÓN STRATEGY: Búsqueda con paginación
                Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
                var searchResults = productService.searchProducts(query, category, pageable);
                
                model.addAttribute("searchResults", searchResults);
                model.addAttribute("query", query);
                model.addAttribute("category", category);
                model.addAttribute("totalResults", searchResults.getTotalElements());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", searchResults.getTotalPages());

                // PATRÓN OBSERVER: Registrar búsqueda
                String userEmail = getCurrentUserEmail();
                logService.logSearchQuery(query, category, userEmail, getClientIP(request));

                log.debug("Búsqueda completada: {} resultados para '{}'", 
                         searchResults.getTotalElements(), query);
            } else {
                model.addAttribute("searchResults", null);
                model.addAttribute("query", "");
            }

            model.addAttribute("pageTitle", "Búsqueda - Proyecto Final UTP");
            model.addAttribute("categories", productService.getAllActiveCategories());

            return "home/search";

        } catch (Exception e) {
            log.error("Error en búsqueda: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error realizando la búsqueda");
            model.addAttribute("pageTitle", "Error en Búsqueda - Proyecto Final UTP");
            return "home/search";
        }
    }

    /**
     * PATRÓN STRATEGY: Manejar errores 404
     */
    @GetMapping("/404")
    public String notFound(Model model, HttpServletRequest request) {
        log.debug("Accediendo a página 404");

        model.addAttribute("pageTitle", "Página No Encontrada - Proyecto Final UTP");
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "La página que buscas no existe");
        
        // PATRÓN OBSERVER: Registrar error 404
        String userEmail = getCurrentUserEmail();
        logService.logPageNotFound(request.getRequestURI(), userEmail, getClientIP(request));

        return "error/404";
    }

    /**
     * PATRÓN STRATEGY: Manejar errores 403 (Acceso denegado)
     */
    @GetMapping("/403")
    public String accessDenied(Model model, HttpServletRequest request) {
        log.debug("Accediendo a página 403");

        model.addAttribute("pageTitle", "Acceso Denegado - Proyecto Final UTP");
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "No tienes permisos para acceder a esta página");
        
        // PATRÓN OBSERVER: Registrar acceso denegado
        String userEmail = getCurrentUserEmail();
        logService.logUnauthorizedAccess(request.getRequestURI(), userEmail, getClientIP(request));

        return "error/403";
    }

    /**
     * PATRÓN STRATEGY: Manejar errores 500
     */
    @GetMapping("/500")
    public String internalError(Model model) {
        log.debug("Accediendo a página 500");

        model.addAttribute("pageTitle", "Error Interno - Proyecto Final UTP");
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Ha ocurrido un error interno del servidor");

        return "error/500";
    }

    /**
     * PATRÓN COMMAND: API endpoint para obtener productos destacados (AJAX)
     */
    @GetMapping("/api/featured-products")
    @ResponseBody
    public List<Product> getFeaturedProducts(@RequestParam(defaultValue = "6") int limit) {
        log.debug("API: Obteniendo {} productos destacados", limit);
        
        try {
            return productService.getFeaturedProducts(limit);
        } catch (Exception e) {
            log.error("Error obteniendo productos destacados via API: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * PATRÓN STRATEGY: Endpoint para estadísticas públicas
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Object> getPublicStats() {
        log.debug("API: Obteniendo estadísticas públicas");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", productService.countActiveProducts());
            stats.put("totalCategories", productService.countActiveCategories());
            stats.put("featuredProducts", productService.countFeaturedProducts());
            stats.put("timestamp", java.time.LocalDateTime.now());
            
            return stats;
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas públicas: {}", e.getMessage());
            throw e;
        }
    }

    // ===== MÉTODOS UTILITARIOS =====

    /**
     * Obtener email del usuario actual
     */
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "anonymous";
    }

    /**
     * Obtener IP del cliente
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    /**
     * PATRÓN STRATEGY: Manejo global de excepciones para este controlador
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model, HttpServletRequest request) {
        log.error("Error en HomeController: {}", e.getMessage(), e);
        
        // PATRÓN OBSERVER: Registrar error
        String userEmail = getCurrentUserEmail();
        logService.logSystemError("HomeController: " + e.getMessage(), 
                                 e.getStackTrace().toString(), "WEB");

        model.addAttribute("pageTitle", "Error - Proyecto Final UTP");
        model.addAttribute("errorMessage", "Ha ocurrido un error inesperado");
        
        return "error/500";
    }
}