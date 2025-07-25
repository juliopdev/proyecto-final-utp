package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.ReservaStockDTO;
// import com.utp.proyectofinal.models.dto.ProductoDTO;
// import com.utp.proyectofinal.models.dto.ClienteDTO;
// import com.utp.proyectofinal.services.ReservaStockService;
// import com.utp.proyectofinal.services.ProductoService;
// import com.utp.proyectofinal.services.ClienteService;
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
@RequestMapping("/reservas-stock")
@RequiredArgsConstructor
public class ReservaStockController {
    
    // private final ReservaStockService reservaStockService;
    // private final ProductoService productoService;
    // private final ClienteService clienteService;
    
    // @GetMapping
    // public String listarReservas(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size,
    //         @RequestParam(defaultValue = "fechaCreacion") String sort,
    //         @RequestParam(defaultValue = "desc") String direction,
    //         @RequestParam(required = false) String estado,
    //         Model model) {
        
    //     Sort sortObj = direction.equals("desc") ? 
    //         Sort.by(sort).descending() : Sort.by(sort).ascending();
    //     Pageable pageable = PageRequest.of(page, size, sortObj);
        
    //     Page<ReservaStockDTO> reservas;
    //     if (estado != null && !estado.trim().isEmpty()) {
    //         reservas = reservaStockService.obtenerPorEstado(estado, pageable);
    //         model.addAttribute("estado", estado);
    //     } else {
    //         reservas = reservaStockService.obtenerTodas(pageable);
    //     }
        
    //     model.addAttribute("reservas", reservas);
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", reservas.getTotalPages());
    //     model.addAttribute("totalElements", reservas.getTotalElements());
    //     model.addAttribute("sort", sort);
    //     model.addAttribute("direction", direction);
        
    //     // Estados para filtros
    //     model.addAttribute("estados", List.of("activa", "confirmada", "liberada", "expirada"));
        
    //     return "reservas-stock/lista";
    // }
    
    // @GetMapping("/nueva")
    // public String mostrarFormularioNueva(Model model) {
    //     List<ProductoDTO> productos = productoService.obtenerTodos();
    //     List<ClienteDTO> clientes = clienteService.obtenerTodos();
        
    //     model.addAttribute("reserva", new ReservaStockDTO());
    //     model.addAttribute("productos", productos);
    //     model.addAttribute("clientes", clientes);
    //     model.addAttribute("titulo", "Nueva Reserva de Stock");
    //     return "reservas-stock/formulario";
    // }
    
    // @PostMapping("/nueva")
    // public String crearReserva(@Valid @ModelAttribute ReservaStockDTO reserva,
    //                          BindingResult result,
    //                          RedirectAttributes redirectAttributes,
    //                          Model model) {
    //     if (result.hasErrors()) {
    //         List<ProductoDTO> productos = productoService.obtenerTodos();
    //         List<ClienteDTO> clientes = clienteService.obtenerTodos();
    //         model.addAttribute("productos", productos);
    //         model.addAttribute("clientes", clientes);
    //         model.addAttribute("titulo", "Nueva Reserva de Stock");
    //         return "reservas-stock/formulario";
    //     }
        
    //     try {
    //         reservaStockService.crear(reserva);
    //         redirectAttributes.addFlashAttribute("mensaje", "Reserva creada exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear la reserva: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/reservas-stock";
    // }
    
    // @GetMapping("/{id}")
    // public String verReserva(@PathVariable String id, Model model) {
    //     try {
    //         ReservaStockDTO reserva = reservaStockService.obtenerPorId(id);
    //         model.addAttribute("reserva", reserva);
    //         return "reservas-stock/detalle";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Reserva no encontrada");
    //         return "error/404";
    //     }
    // }
    
    // @PostMapping("/{id}/confirmar")
    // public String confirmarReserva(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         reservaStockService.confirmar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Reserva confirmada exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al confirmar reserva: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/reservas-stock/" + id;
    // }
    
    // @PostMapping("/{id}/liberar")
    // public String liberarReserva(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         reservaStockService.liberar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Reserva liberada exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al liberar reserva: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/reservas-stock/" + id;
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarReserva(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         reservaStockService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Reserva eliminada exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar reserva: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/reservas-stock";
    // }
}