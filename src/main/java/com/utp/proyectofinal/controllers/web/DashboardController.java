package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    // private final PedidoService pedidoService;
    // private final ClienteService clienteService;
    // private final ProductoService productoService;
    // private final CategoriaService categoriaService;
    // private final ComentarioService comentarioService;
    
    // @GetMapping
    // public String mostrarDashboard(Model model) {
    //     try {
    //         // Estadísticas generales
    //         long totalPedidos = pedidoService.contarTodos();
    //         long totalClientes = clienteService.contarTodos();
    //         long totalProductos = productoService.contarTodos();
    //         long totalCategorias = categoriaService.contarTodos();
            
    //         // Pedidos por estado
    //         long pedidosPendientes = pedidoService.contarPorEstado("pendiente");
    //         long pedidosConfirmados = pedidoService.contarPorEstado("confirmado");
    //         long pedidosEnPreparacion = pedidoService.contarPorEstado("en_preparacion");
    //         long pedidosEntregados = pedidoService.contarPorEstado("entregado");
            
    //         // Últimos pedidos
    //         var ultimosPedidos = pedidoService.obtenerUltimos(5);
            
    //         // Productos con stock bajo
    //         var productosStockBajo = productoService.obtenerConStockBajo(10);
            
    //         // Comentarios recientes
    //         var comentariosRecientes = comentarioService.obtenerRecientes(5);
            
    //         model.addAttribute("totalPedidos", totalPedidos);
    //         model.addAttribute("totalClientes", totalClientes);
    //         model.addAttribute("totalProductos", totalProductos);
    //         model.addAttribute("totalCategorias", totalCategorias);
            
    //         model.addAttribute("pedidosPendientes", pedidosPendientes);
    //         model.addAttribute("pedidosConfirmados", pedidosConfirmados);
    //         model.addAttribute("pedidosEnPreparacion", pedidosEnPreparacion);
    //         model.addAttribute("pedidosEntregados", pedidosEntregados);
            
    //         model.addAttribute("ultimosPedidos", ultimosPedidos);
    //         model.addAttribute("productosStockBajo", productosStockBajo);
    //         model.addAttribute("comentariosRecientes", comentariosRecientes);
            
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
    //     }
        
    //     return "dashboard/index";
    // }
}