package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.CategoriaDTO;
// import com.utp.proyectofinal.services.CategoriaService;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {
    
    // private final CategoriaService categoriaService;
    
    // @PostMapping("/crear")
    // public String crearCategoria(@Valid @ModelAttribute CategoriaDTO categoria,
    //                            BindingResult result,
    //                            RedirectAttributes redirectAttributes,
    //                            Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Nueva Categoría");
    //         return "categorias/formulario";
    //     }
        
    //     try {
    //         categoriaService.crear(categoria);
    //         redirectAttributes.addFlashAttribute("mensaje", "Categoría creada exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear la categoría: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return ""; // retorna la categoria creada
    // }
    
    // @PostMapping("/{id}/editar")
    // public String actualizarCategoria(@PathVariable String id,
    //                                 @Valid @ModelAttribute CategoriaDTO categoria,
    //                                 BindingResult result,
    //                                 RedirectAttributes redirectAttributes,
    //                                 Model model) {
    //     if (result.hasErrors()) {
    //         model.addAttribute("titulo", "Editar Categoría");
    //         return "categorias/formulario";
    //     }
        
    //     try {
    //         categoria.setId(id);
    //         categoriaService.actualizar(categoria);
    //         redirectAttributes.addFlashAttribute("mensaje", "Categoría actualizada exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar la categoría: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return ""; // retorna la categoria editada
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarCategoria(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         categoriaService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Categoría eliminada exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar la categoría: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return ""; // retorna un booleano de confirmacion de la eliminacion
    // }
}