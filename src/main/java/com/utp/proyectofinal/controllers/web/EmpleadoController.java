package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.EmpleadoDTO;
// import com.utp.proyectofinal.models.dto.ClienteDTO;
// import com.utp.proyectofinal.services.EmpleadoService;
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
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {
    
    // private final EmpleadoService empleadoService;
    // private final ClienteService clienteService;
    
    // @GetMapping
    // public String listarEmpleados(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size,
    //         @RequestParam(defaultValue = "fechaCreacion") String sort,
    //         @RequestParam(defaultValue = "desc") String direction,
    //         Model model) {
        
    //     Sort sortObj = direction.equals("desc") ? 
    //         Sort.by(sort).descending() : Sort.by(sort).ascending();
    //     Pageable pageable = PageRequest.of(page, size, sortObj);
        
    //     Page<EmpleadoDTO> empleados = empleadoService.obtenerTodos(pageable);
        
    //     model.addAttribute("empleados", empleados);
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", empleados.getTotalPages());
    //     model.addAttribute("totalElements", empleados.getTotalElements());
    //     model.addAttribute("sort", sort);
    //     model.addAttribute("direction", direction);
        
    //     return "empleados/lista";
    // }
    
    // @GetMapping("/nuevo")
    // public String mostrarFormularioNuevo(Model model) {
    //     List<ClienteDTO> usuarios = clienteService.obtenerPorRol("empleado");
    //     model.addAttribute("empleado", new EmpleadoDTO());
    //     model.addAttribute("usuarios", usuarios);
    //     model.addAttribute("titulo", "Nuevo Empleado");
    //     return "empleados/formulario";
    // }
    
    // @PostMapping("/nuevo")
    // public String crearEmpleado(@Valid @ModelAttribute EmpleadoDTO empleado,
    //                           BindingResult result,
    //                           RedirectAttributes redirectAttributes,
    //                           Model model) {
    //     if (result.hasErrors()) {
    //         List<ClienteDTO> usuarios = clienteService.obtenerPorRol("empleado");
    //         model.addAttribute("usuarios", usuarios);
    //         model.addAttribute("titulo", "Nuevo Empleado");
    //         return "empleados/formulario";
    //     }
        
    //     try {
    //         empleadoService.crear(empleado);
    //         redirectAttributes.addFlashAttribute("mensaje", "Empleado creado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al crear empleado: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/empleados";
    // }
    
    // @GetMapping("/{id}")
    // public String verEmpleado(@PathVariable String id, Model model) {
    //     try {
    //         EmpleadoDTO empleado = empleadoService.obtenerPorId(id);
    //         model.addAttribute("empleado", empleado);
    //         return "empleados/detalle";
    //     } catch (Exception e) {
    //         model.addAttribute("error", "Empleado no encontrado");
    //         return "error/404";
    //     }
    // }
    
    // @PostMapping("/{id}/eliminar")
    // public String eliminarEmpleado(@PathVariable String id, RedirectAttributes redirectAttributes) {
    //     try {
    //         empleadoService.eliminar(id);
    //         redirectAttributes.addFlashAttribute("mensaje", "Empleado eliminado exitosamente");
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "success");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar empleado: " + e.getMessage());
    //         redirectAttributes.addFlashAttribute("tipoMensaje", "error");
    //     }
        
    //     return "redirect:/empleados";
    // }
}