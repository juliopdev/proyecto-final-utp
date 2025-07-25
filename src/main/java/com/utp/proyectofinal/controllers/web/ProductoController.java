package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.ProductoDTO;
// import com.utp.proyectofinal.models.dto.CategoriaDTO;
// import com.utp.proyectofinal.services.ProductoService;
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

// import java.util.List;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {
    
    // private final ProductoService productoService;
    // private final CategoriaService categoriaService;
    
    // @GetMapping
    // public String listarProductos(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "12") int size,
    //         @RequestParam(defaultValue = "nombre") String sort,
    //         @RequestParam(defaultValue = "asc") String direction,
    //         @RequestParam(required = false) String buscar,
    //         @RequestParam(required = false) String categoria,
    //         Model model) {
        
    //     Sort sortObj = direction.equals("desc") ? 
    //         Sort.by(sort).descending() : Sort.by(sort).ascending();
    //     Pageable pageable = PageRequest.of(page, size, sortObj);
        
    //     Page<ProductoDTO> productos;
    //     if (buscar != null && !buscar.trim().isEmpty()) {
    //         productos = productoService.buscarPorNombre(buscar, pageable);
    //         model.addAttribute("buscar", buscar);
    //     } else if (categoria != null && !categoria.trim().isEmpty()) {
    //         productos = productoService.obtenerPorCategoria(categoria, pageable);
    //         model.addAttribute("categoria", categoria);
    //     } else {
    //         productos = productoService.obtenerTodos(pageable);
    //     }
        
    //     List<CategoriaDTO> categorias = categoriaService.obtenerTodas();
        
    //     model.addAttribute("productos", productos);
    //     model.addAttribute("categorias", categorias);
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", productos.getTotalPages());
    //     model.addAttribute("totalElements", productos.getTotalElements());
    //     model.addAttribute("sort", sort);
    //     model.addAttribute("direction", direction);
        
    //     return "productos/lista";
    // }
    
    // @GetMapping("/nuevo")
    // public String mostrarFormularioNuevo(Model model) {
    //     List<CategoriaDTO> categorias = categoriaService.obtenerTodas();
    //     model.addAttribute("producto", new ProductoDTO());
    //     model.addAttribute("categorias", categorias);
    //     model.addAttribute("titulo", "Nuevo Producto");
    //     return "productos/formulario";
    // }
    
    // @PostMapping("/nuevo")
    // public String crearProducto(@Valid @ModelAttribute ProductoDTO producto,
    //                           BindingResult result,
    //                           RedirectAttributes redirectAttributes,
    //                           Model model) {
    //     if (result.hasErrors()) {
    //         List<CategoriaDTO> categorias = categoriaService.obtenerTodas();
    //         model.addAttribute("categorias", categorias);
    //         model.addAttribute("titulo", "Nuevo Producto");
    //         return "productos/formulario";
    //     }
        
    //     try {
    //         productoService.crear(producto);
    //         redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear el producto: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/productos";
    // }
    
    // @GetMapping("/{id}")
    // public String verProducto(@PathVariable String id, Model model) {
    //     try {
    //         ProductoDTO producto = productoService.obtenerPorId(id);
    //         model.addAttribute("producto", producto);
    //         return "productos/detalle";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Producto no encontrado");
    //         return "error/404";
    //     }
    // }
    
    // @GetMapping("/{id}/editar")
    // public String mostrarFormularioEditar(@PathVariable String id, Model model) {
    //     try {
    //         ProductoDTO producto = productoService.obtenerPorId(id);
    //         List<CategoriaDTO> categorias = categoriaService.obtenerTodas();
    //         model.addAttribute("producto", producto);
    //         model.addAttribute("categorias", categorias);
    //         model.addAttribute("titulo", "Editar Producto");
    //         return "productos/formulario";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Producto no encontrado");
    //         return "error/404";
    //     }
    // }
    
    // @PostMapping("/{id}/editar")
    // public String actualizarProducto(@PathVariable String id,
    //                                @Valid @ModelAttribute ProductoDTO producto,
    //                                BindingResult result,
    //                                RedirectAttributes redirectAttributes,
    //                                Model model) {
    //     if (result.hasErrors()) {
    //         List<CategoriaDTO> categorias = categoriaService.obtenerTodas();
    //         model.addAttribute("categorias", categorias);
    //         model.addAttribute("titulo", "Editar Producto");
    //         return "productos/formulario";
    //     }
        
    //     try {
    //         producto.setId(id);
    //         productoService.actualizar(producto);
    //         redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el producto: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/productos";
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarProducto(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         productoService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el producto: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/productos";
    // }
}