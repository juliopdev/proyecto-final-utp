package com.utp.proyectofinal.controllers.web;

import com.utp.proyectofinal.models.dto.UpdateProfileRequest;
import com.utp.proyectofinal.models.dto.UserProfileResponse;
import com.utp.proyectofinal.models.entities.User;
import com.utp.proyectofinal.services.interfaces.UserService;
import com.utp.proyectofinal.services.interfaces.LogService;
import com.utp.proyectofinal.services.interfaces.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para el dashboard de usuario autenticado
 * 
 * PATRONES IMPLEMENTADOS:
 * - MVC: Patrón Model-View-Controller para área privada
 * - COMMAND: Cada método representa un comando de gestión
 * - STRATEGY: Diferentes vistas según rol de usuario
 * - FACADE: Coordina múltiples servicios para el dashboard
 * - OBSERVER: Registra todas las acciones del usuario
 * - TEMPLATE METHOD: Templates comunes para área privada
 * 
 * @author Julio Pariona
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class DashboardController {

    private final UserService userService;
    private final LogService logService;
    private final OrderService orderService;

    /**
     * PATRÓN TEMPLATE METHOD: Template principal del dashboard
     */
    @GetMapping
    public String dashboard(Model model, HttpServletRequest request) {
        log.debug("Accediendo al dashboard principal");

        try {
            User currentUser = getCurrentUser();
            
            // PATRÓN FACADE: Obtener todos los datos para el dashboard
            UserProfileResponse userProfile = userService.getUserProfile(currentUser.getId());
            Map<String, Object> userStats = userService.getUserStatistics(currentUser.getId());
            List<Map<String, Object>> recentActivity = userService.getUserRecentActivity(currentUser.getId(), 10);
            
            // Obtener pedidos recientes si el servicio existe
            // List<Order> recentOrders = orderService.getUserRecentOrders(currentUser.getId(), 5);
            
            // PATRÓN STRATEGY: Contenido específico según rol
            String roleBasedContent = getRoleBasedDashboardContent(currentUser.getRole().getName().name());

            // Preparar modelo para la vista
            model.addAttribute("user", currentUser);
            model.addAttribute("userProfile", userProfile);
            model.addAttribute("userStats", userStats);
            model.addAttribute("recentActivity", recentActivity);
            // model.addAttribute("recentOrders", recentOrders);
            model.addAttribute("roleContent", roleBasedContent);
            model.addAttribute("pageTitle", "Dashboard - " + currentUser.getFullName());
            model.addAttribute("welcomeMessage", "¡Bienvenido, " + currentUser.getFirstName() + "!");

            // PATRÓN OBSERVER: Registrar acceso al dashboard
            logService.logDashboardAccess(currentUser.getEmail(), getClientIP(request));

            log.debug("Dashboard cargado exitosamente para usuario: {}", currentUser.getEmail());
            return "dashboard/index";

        } catch (Exception e) {
            log.error("Error cargando dashboard: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error cargando el dashboard");
            return "error/500";
        }
    }

    /**
     * PATRÓN COMMAND: Comando para ver perfil de usuario
     */
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        log.debug("Visualizando perfil de usuario");

        try {
            User currentUser = getCurrentUser();
            UserProfileResponse userProfile = userService.getUserProfile(currentUser.getId());
            
            model.addAttribute("user", currentUser);
            model.addAttribute("userProfile", userProfile);
            model.addAttribute("pageTitle", "Mi Perfil - " + currentUser.getFullName());
            
            return "dashboard/profile/view";

        } catch (Exception e) {
            log.error("Error cargando perfil: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error cargando el perfil");
            return "dashboard/index";
        }
    }

    /**
     * PATRÓN COMMAND: Mostrar formulario de edición de perfil
     */
    @GetMapping("/profile/edit")
    public String editProfileForm(Model model) {
        log.debug("Mostrando formulario de edición de perfil");

        try {
            User currentUser = getCurrentUser();
            
            // PATRÓN ADAPTER: Convertir User a UpdateProfileRequest
            UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .phone(currentUser.getPhone())
                .birthDate(currentUser.getBirthDate())
                .address(currentUser.getAddress())
                .build();

            model.addAttribute("updateRequest", updateRequest);
            model.addAttribute("user", currentUser);
            model.addAttribute("pageTitle", "Editar Perfil - " + currentUser.getFullName());
            
            return "dashboard/profile/edit";

        } catch (Exception e) {
            log.error("Error mostrando formulario de edición: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error cargando el formulario");
            return "dashboard/profile/view";
        }
    }

    /**
     * PATRÓN COMMAND: Procesar actualización de perfil
     */
    @PostMapping("/profile/edit")
    public String updateProfile(
            @Valid @ModelAttribute UpdateProfileRequest updateRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request) {
        
        log.debug("Procesando actualización de perfil");

        if (bindingResult.hasErrors()) {
            log.warn("Errores de validación en actualización de perfil: {}", bindingResult.getAllErrors());
            User currentUser = getCurrentUser();
            model.addAttribute("user", currentUser);
            model.addAttribute("pageTitle", "Editar Perfil - " + currentUser.getFullName());
            return "dashboard/profile/edit";
        }

        try {
            User currentUser = getCurrentUser();
            
            // PATRÓN COMMAND: Actualizar perfil
            User updatedUser = userService.updateUserProfile(currentUser.getId(), updateRequest, getClientIP(request));
            
            // PATRÓN OBSERVER: Registrar actualización
            logService.logProfileUpdate(updatedUser.getEmail(), getClientIP(request));
            
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente");
            log.info("Perfil actualizado exitosamente para usuario: {}", updatedUser.getEmail());
            
            return "redirect:/dashboard/profile";

        } catch (Exception e) {
            log.error("Error actualizando perfil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error actualizando el perfil: " + e.getMessage());
            return "redirect:/dashboard/profile/edit";
        }
    }

    /**
     * PATRÓN COMMAND: Ver configuraciones de cuenta
     */
    @GetMapping("/settings")
    public String accountSettings(Model model) {
        log.debug("Accediendo a configuraciones de cuenta");

        try {
            User currentUser = getCurrentUser();
            Map<String, Boolean> notificationPrefs = userService.getNotificationPreferences(currentUser.getId());
            
            model.addAttribute("user", currentUser);
            model.addAttribute("notificationPreferences", notificationPrefs);
            model.addAttribute("pageTitle", "Configuraciones - " + currentUser.getFullName());
            
            return "dashboard/settings/index";

        } catch (Exception e) {
            log.error("Error cargando configuraciones: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error cargando las configuraciones");
            return "dashboard/index";
        }
    }

    /**
     * PATRÓN COMMAND: Actualizar preferencias de notificación
     */
    @PostMapping("/settings/notifications")
    public String updateNotificationPreferences(
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        
        log.debug("Actualizando preferencias de notificación");

        try {
            User currentUser = getCurrentUser();
            
            // PATRÓN ADAPTER: Convertir parámetros a Map<String, Boolean>
            Map<String, Boolean> preferences = params.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("notification_"))
                .collect(java.util.stream.Collectors.toMap(
                    entry -> entry.getKey().replace("notification_", ""),
                    entry -> "on".equals(entry.getValue()) || "true".equals(entry.getValue())
                ));

            // PATRÓN COMMAND: Actualizar preferencias
            userService.updateNotificationPreferences(currentUser.getId(), preferences);
            
            // PATRÓN OBSERVER: Registrar cambio
            logService.logNotificationPreferencesUpdate(currentUser.getEmail(), getClientIP(request));
            
            redirectAttributes.addFlashAttribute("successMessage", "Preferencias actualizadas exitosamente");
            log.info("Preferencias de notificación actualizadas para: {}", currentUser.getEmail());
            
            return "redirect:/dashboard/settings";

        } catch (Exception e) {
            log.error("Error actualizando preferencias: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error actualizando preferencias");
            return "redirect:/dashboard/settings";
        }
    }

    /**
     * PATRÓN COMMAND: Ver historial de actividad
     */
    @GetMapping("/activity")
    public String viewActivity(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {
        
        log.debug("Visualizando historial de actividad - página: {}", page);

        try {
            User currentUser = getCurrentUser();
            List<Map<String, Object>> activities = userService.getUserRecentActivity(currentUser.getId(), size);
            
            model.addAttribute("user", currentUser);
            model.addAttribute("activities", activities);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageTitle", "Actividad - " + currentUser.getFullName());
            
            return "dashboard/activity/index";

        } catch (Exception e) {
            log.error("Error cargando actividad: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error cargando el historial");
            return "dashboard/index";
        }
    }

    /**
     * PATRÓN STRATEGY: Dashboard específico para administradores
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model, HttpServletRequest request) {
        log.debug("Accediendo al dashboard de administrador");

        try {
            User currentUser = getCurrentUser();
            
            // PATRÓN FACADE: Obtener estadísticas administrativas
            Map<String, Object> systemStats = userService.getGeneralUserStatistics();
            Map<String, Object> logStats = logService.getSystemMetrics();
            
            model.addAttribute("user", currentUser);
            model.addAttribute("systemStats", systemStats);
            model.addAttribute("logStats", logStats);
            model.addAttribute("pageTitle", "Panel Administrativo - " + currentUser.getFullName());
            
            // PATRÓN OBSERVER: Registrar acceso admin
            logService.logAdminDashboardAccess(currentUser.getEmail(), getClientIP(request));
            
            return "dashboard/admin/index";

        } catch (Exception e) {
            log.error("Error cargando dashboard admin: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error cargando panel administrativo");
            return "dashboard/index";
        }
    }

    /**
     * PATRÓN COMMAND: API para obtener notificaciones del usuario
     */
    @GetMapping("/api/notifications")
    @ResponseBody
    public List<Map<String, Object>> getUserNotifications(
            @RequestParam(value = "unread", defaultValue = "false") boolean unreadOnly) {
        
        log.debug("Obteniendo notificaciones - solo no leídas: {}", unreadOnly);

        try {
            User currentUser = getCurrentUser();
            return userService.getUserNotifications(currentUser.getId(), unreadOnly);

        } catch (Exception e) {
            log.error("Error obteniendo notificaciones: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * PATRÓN COMMAND: API para marcar notificaciones como leídas
     */
    @PostMapping("/api/notifications/mark-read")
    @ResponseBody
    public Map<String, Object> markNotificationsRead(@RequestBody List<String> notificationIds) {
        log.debug("Marcando {} notificaciones como leídas", notificationIds.size());

        try {
            User currentUser = getCurrentUser();
            userService.markNotificationsAsRead(currentUser.getId(), notificationIds);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Notificaciones marcadas como leídas",
                "count", notificationIds.size()
            );
            
            return response;

        } catch (Exception e) {
            log.error("Error marcando notificaciones: {}", e.getMessage());
            return Map.of(
                "success", false,
                "message", "Error procesando notificaciones"
            );
        }
    }

    // ===== MÉTODOS UTILITARIOS =====

    /**
     * PATRÓN STRATEGY: Obtener contenido específico según rol
     */
    private String getRoleBasedDashboardContent(String roleName) {
        switch (roleName.toUpperCase()) {
            case "ADMIN":
                return "admin";
            case "VENDEDOR":
                return "seller";
            case "REPARTIDOR":
                return "delivery";
            case "USER":
            default:
                return "user";
        }
    }

    /**
     * Obtener usuario actual autenticado
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.error("Usuario autenticado no encontrado en BD: {}", email);
            throw new RuntimeException("Usuario no encontrado");
        }
        
        return userOpt.get();
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
     * PATRÓN STRATEGY: Manejo de excepciones del dashboard
     */
    @ExceptionHandler(Exception.class)
    public String handleDashboardException(Exception e, Model model) {
        log.error("Error en DashboardController: {}", e.getMessage(), e);
        
        try {
            // PATRÓN OBSERVER: Registrar error
            User currentUser = getCurrentUser();
            logService.logSystemError("DashboardController: " + e.getMessage(), 
                                     e.getStackTrace().toString(), "WEB");
        } catch (Exception logError) {
            log.error("Error adicional registrando error: {}", logError.getMessage());
        }

        model.addAttribute("pageTitle", "Error - Dashboard");
        model.addAttribute("errorMessage", "Ha ocurrido un error en el dashboard");
        
        return "error/500";
    }
}