package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.MetodoPagoDTO;
// import com.utp.proyectofinal.services.MetodoPagoService;
// import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/metodos-pago")
@RequiredArgsConstructor
public class MetodoPagoController {
    
    // private final MetodoPagoService metodoPagoService;
    
    // @GetMapping
    // public String listarMetodosPago(Model model) {
    //     List<MetodoPagoDTO> metodos = metodoPagoService.obtenerTodos();
    //     model.addAttribute("metodos", metodos);
    //     return "metodos-pago/lista";
    // }
    
    // @GetMapping("/nuevo")
    // public String mostrarFormularioNuevo(Model model) {
    //     model.addAttribute("metodo", new MetodoPagoDTO());
    //     model.addAttribute("titulo", "Nuevo Método de Pago");
    //     return "metodos-pago/formulario";
    // }
    
    // @PostMapping("/nuevo")
    // public String crearMetodo(@Valid @ModelAttribute MetodoPagoDTO metodo,
    //                         BindingResult result,
    //                         RedirectAttributes redirectAttributes,
    //                         Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Nuevo Método de Pago");
    //         return "metodos-pago/formulario";
    //     }
        
    //     try {
    //         metodoPagoService.crear(metodo);
    //         redirectAttributes.addFlashAttribute("mensaje", "Método de pago creado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear el método: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/metodos-pago";
    // }
    
    // @GetMapping("/{id}/editar")
    // public String mostrarFormularioEditar(@PathVariable String id, Model model) {
    //     try {
    //         MetodoPagoDTO metodo = metodoPagoService.obtenerPorId(id);
    //         model.addAttribute("metodo", metodo);
    //         model.addAttribute("titulo", "Editar Método de Pago");
    //         return "metodos-pago/formulario";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Método no encontrado");
    //         return "error/404";
    //     }
    // }
    
    // @PostMapping("/{id}/editar")
    // public String actualizarMetodo(@PathVariable String id,
    //                              @Valid @ModelAttribute MetodoPagoDTO metodo,
    //                              BindingResult result,
    //                              RedirectAttributes redirectAttributes,
    //                              Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Editar Método de Pago");
    //         return "metodos-pago/formulario";
    //     }
        
    //     try {
    //         metodo.setId(id);
    //         metodoPagoService.actualizar(metodo);
    //         redirectAttributes.addFlashAttribute("mensaje", "Método actualizado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el método: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/metodos-pago";
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarMetodo(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         metodoPagoService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Método eliminado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el método: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/metodos-pago";
    // }
}