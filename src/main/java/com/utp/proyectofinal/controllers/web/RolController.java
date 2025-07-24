package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.RolDTO;
// import com.utp.proyectofinal.services.RolService;
// import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolController {
    
    // private final RolService rolService;
    
    // @GetMapping
    // public String listarRoles(Model model) {
    //     List<RolDTO> roles = rolService.obtenerTodos();
    //     model.addAttribute("roles", roles);
    //     return "roles/lista";
    // }
    
    // @GetMapping("/nuevo")
    // public String mostrarFormularioNuevo(Model model) {
    //     model.addAttribute("rol", new RolDTO());
    //     model.addAttribute("titulo", "Nuevo Rol");
    //     return "roles/formulario";
    // }
    
    // @PostMapping("/nuevo")
    // public String crearRol(@Valid @ModelAttribute RolDTO rol,
    //                      BindingResult result,
    //                      RedirectAttributes redirectAttributes,
    //                      Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Nuevo Rol");
    //         return "roles/formulario";
    //     }
        
    //     try {
    //         rolService.crear(rol);
    //         redirectAttributes.addFlashAttribute("mensaje", "Rol creado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear el rol: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/roles";
    // }
    
    // @GetMapping("/{id}/editar")
    // public String mostrarFormularioEditar(@PathVariable String id, Model model) {
    //     try {
    //         RolDTO rol = rolService.obtenerPorId(id);
    //         model.addAttribute("rol", rol);
    //         model.addAttribute("titulo", "Editar Rol");
    //         return "roles/formulario";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Rol no encontrado");
    //         return "error/404";
    //     }
    // }
    
    // @PostMapping("/{id}/editar")
    // public String actualizarRol(@PathVariable String id,
    //                           @Valid @ModelAttribute RolDTO rol,
    //                           BindingResult result,
    //                           RedirectAttributes redirectAttributes,
    //                           Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Editar Rol");
    //         return "roles/formulario";
    //     }
        
    //     try {
    //         rol.setId(id);
    //         rolService.actualizar(rol);
    //         redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el rol: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/roles";
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarRol(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         rolService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Rol eliminado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el rol: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/roles";
    // }
}