package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.EstadoDTO;
// import com.utp.proyectofinal.services.EstadoService;
// import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import java.util.List;

@Controller
@RequestMapping("/estados")
@RequiredArgsConstructor
public class EstadoController {
    
    // private final EstadoService estadoService;
    
    // @GetMapping
    // public String listarEstados(Model model) {
    //     List<EstadoDTO> estados = estadoService.obtenerTodos();
    //     model.addAttribute("estados", estados);
    //     return "estados/lista";
    // }
    
    // @GetMapping("/nuevo")
    // public String mostrarFormularioNuevo(Model model) {
    //     model.addAttribute("estado", new EstadoDTO());
    //     model.addAttribute("titulo", "Nuevo Estado");
    //     return "estados/formulario";
    // }
    
    // @PostMapping("/nuevo")
    // public String crearEstado(@Valid @ModelAttribute EstadoDTO estado,
    //                         BindingResult result,
    //                         RedirectAttributes redirectAttributes,
    //                         Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Nuevo Estado");
    //         return "estados/formulario";
    //     }
        
    //     try {
    //         estadoService.crear(estado);
    //         redirectAttributes.addFlashAttribute("mensaje", "Estado creado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear el estado: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/estados";
    // }
    
    // @GetMapping("/{id}/editar")
    // public String mostrarFormularioEditar(@PathVariable String id, Model model) {
    //     try {
    //         EstadoDTO estado = estadoService.obtenerPorId(id);
    //         model.addAttribute("estado", estado);
    //         model.addAttribute("titulo", "Editar Estado");
    //         return "estados/formulario";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Estado no encontrado");
    //         return "error/404";
    //     }
    // }
    
    // @PostMapping("/{id}/editar")
    // public String actualizarEstado(@PathVariable String id,
    //                              @Valid @ModelAttribute EstadoDTO estado,
    //                              BindingResult result,
    //                              RedirectAttributes redirectAttributes,
    //                              Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Editar Estado");
    //         return "estados/formulario";
    //     }
        
    //     try {
    //         estado.setId(id);
    //         estadoService.actualizar(estado);
    //         redirectAttributes.addFlashAttribute("mensaje", "Estado actualizado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el estado: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/estados";
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarEstado(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         estadoService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Estado eliminado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el estado: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/estados";
    // }
}