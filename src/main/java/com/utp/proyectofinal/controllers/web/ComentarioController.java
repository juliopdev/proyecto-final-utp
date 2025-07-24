package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.ComentarioDTO;
// import com.utp.proyectofinal.services.ComentarioService;
// import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comentarios")
@RequiredArgsConstructor
public class ComentarioController {
    
    // private final ComentarioService comentarioService;
    
    // @GetMapping
    // public String listarComentarios(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size,
    //         @RequestParam(defaultValue = "fechaCreacion") String sort,
    //         @RequestParam(defaultValue = "desc") String direction,
    //         @RequestParam(required = false) Integer puntuacion,
    //         Model model) {
        
    //     Sort sortObj = direction.equals("desc") ? 
    //         Sort.by(sort).descending() : Sort.by(sort).ascending();
    //     Pageable pageable = PageRequest.of(page, size, sortObj);
        
    //     Page<ComentarioDTO> comentarios;
    //     if (puntuacion != null && puntuacion > 0) {
    //         comentarios = comentarioService.obtenerPorPuntuacion(puntuacion, pageable);
    //         model.addAttribute("puntuacion", puntuacion);
    //     } else {
    //         comentarios = comentarioService.obtenerTodos(pageable);
    //     }
        
    //     model.addAttribute("comentarios", comentarios);
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", comentarios.getTotalPages());
    //     model.addAttribute("totalElements", comentarios.getTotalElements());
    //     model.addAttribute("sort", sort);
    //     model.addAttribute("direction", direction);
        
    //     return "comentarios/lista";
    // }
    
    // @GetMapping("/nuevo")
    // public String mostrarFormularioNuevo(Model model) {
    //     model.addAttribute("comentario", new ComentarioDTO());
    //     model.addAttribute("titulo", "Nuevo Comentario");
    //     return "comentarios/formulario";
    // }
    
    // @PostMapping("/nuevo")
    // public String crearComentario(@Valid @ModelAttribute ComentarioDTO comentario,
    //                             BindingResult result,
    //                             RedirectAttributes redirectAttributes,
    //                             Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Nuevo Comentario");
    //         return "comentarios/formulario";
    //     }
        
    //     try {
    //         comentarioService.crear(comentario);
    //         redirectAttributes.addFlashAttribute("mensaje", "Comentario creado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear el comentario: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/comentarios";
    // }
    
    // @GetMapping("/{id}")
    // public String verComentario(@PathVariable String id, Model model) {
    //     try {
    //         ComentarioDTO comentario = comentarioService.obtenerPorId(id);
    //         model.addAttribute("comentario", comentario);
    //         return "comentarios/detalle";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Comentario no encontrado");
    //         return "error/404";
    //     }
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarComentario(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         comentarioService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Comentario eliminado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el comentario: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/comentarios";
    // }
}