package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.PedidoDTO;
// import com.utp.proyectofinal.models.dto.ClienteDTO;
// import com.utp.proyectofinal.services.PedidoService;
// import com.utp.proyectofinal.services.ClienteService;
// import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import java.util.List;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    
    // private final PedidoService pedidoService;
    // private final ClienteService clienteService;
    
    // @GetMapping
    // public String listarPedidos(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size,
    //         @RequestParam(defaultValue = "fechaCreacion") String sort,
    //         @RequestParam(defaultValue = "desc") String direction,
    //         @RequestParam(required = false) String estado,
    //         @RequestParam(required = false) String tipoEntrega,
    //         Model model) {
        
    //     Sort sortObj = direction.equals("desc") ? 
    //         Sort.by(sort).descending() : Sort.by(sort).ascending();
    //     Pageable pageable = PageRequest.of(page, size, sortObj);
        
    //     Page<PedidoDTO> pedidos;
    //     if (estado != null && !estado.trim().isEmpty()) {
    //         pedidos = pedidoService.obtenerPorEstado(estado, pageable);
    //         model.addAttribute("estado", estado);
    //     } else if (tipoEntrega != null && !tipoEntrega.trim().isEmpty()) {
    //         pedidos = pedidoService.obtenerPorTipoEntrega(tipoEntrega, pageable);
    //         model.addAttribute("tipoEntrega", tipoEntrega);
    //     } else {
    //         pedidos = pedidoService.obtenerTodos(pageable);
    //     }
        
    //     model.addAttribute("pedidos", pedidos);
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", pedidos.getTotalPages());
    //     model.addAttribute("totalElements", pedidos.getTotalElements());
    //     model.addAttribute("sort", sort);
    //     model.addAttribute("direction", direction);
        
    //     // Estados y tipos de entrega para filtros
    //     model.addAttribute("estados", List.of("pendiente", "confirmado", "en_preparacion", "en_camino", "entregado", "cancelado"));
    //     model.addAttribute("tiposEntrega", List.of("delivery", "recojo", "mesa"));
        
    //     return "pedidos/lista";
    // }
    
    // @GetMapping("/nuevo")
    // public String mostrarFormularioNuevo(Model model) {
    //     List<ClienteDTO> clientes = clienteService.obtenerTodos();
    //     model.addAttribute("pedido", new PedidoDTO());
    //     model.addAttribute("clientes", clientes);
    //     model.addAttribute("titulo", "Nuevo Pedido");
    //     return "pedidos/formulario";
    // }
    
    // @PostMapping("/nuevo")
    // public String crearPedido(@Valid @ModelAttribute PedidoDTO pedido,
    //                         BindingResult result,
    //                         RedirectAttributes redirectAttributes,
    //                         Model model) {
    //     if (result.hasErrors()) {
    //         List<ClienteDTO> clientes = clienteService.obtenerTodos();
    //         model.addAttribute("clientes", clientes);
    //         model.addAttribute("titulo", "Nuevo Pedido");
    //         return "pedidos/formulario";
    //     }
        
    //     try {
    //         pedidoService.crear(pedido);
    //         redirectAttributes.addFlashAttribute("mensaje", "Pedido creado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear el pedido: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/pedidos";
    // }
    
    // @GetMapping("/{id}")
    // public String verPedido(@PathVariable String id, Model model) {
    //     try {
    //         PedidoDTO pedido = pedidoService.obtenerPorId(id);
    //         model.addAttribute("pedido", pedido);
    //         return "pedidos/detalle";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Pedido no encontrado");
    //         return "error/404";
    //     }
    // }
    
    // @PostMapping("/{id}/estado")
    // public String actualizarEstado(@PathVariable String id,
    //                              @RequestParam String nuevoEstado,
    //                              RedirectAttributes redirectAttributes) {
    //     try {
    //         pedidoService.actualizarEstado(id, nuevoEstado);
    //         redirectAttributes.addFlashAttribute("mensaje", "Estado del pedido actualizado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el estado: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/pedidos/" + id;
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarPedido(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         pedidoService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Pedido eliminado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el pedido: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/pedidos";
    // }
}