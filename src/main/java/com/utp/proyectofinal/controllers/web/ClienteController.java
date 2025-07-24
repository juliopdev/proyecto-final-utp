package com.utp.proyectofinal.controllers.web;

// import com.utp.proyectofinal.models.dto.ClienteDTO;
// import com.utp.proyectofinal.models.dto.RolDTO;
// import com.utp.proyectofinal.services.ClienteService;
// import com.utp.proyectofinal.services.RolService;
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
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {
    
    // private final ClienteService clienteService;
    // private final RolService rolService;
    
    // @GetMapping
    // public String listarClientes(
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size,
    //         @RequestParam(defaultValue = "nombre") String sort,
    //         @RequestParam(defaultValue = "asc") String direction,
    //         @RequestParam(required = false) String buscar,
    //         Model model) {
        
    //     Sort sortObj = direction.equals("desc") ? 
    //         Sort.by(sort).descending() : Sort.by(sort).ascending();
    //     Pageable pageable = PageRequest.of(page, size, sortObj);
        
    //     Page<ClienteDTO> clientes;
    //     if (buscar != null && !buscar.trim().isEmpty()) {
    //         clientes = clienteService.buscarPorNombreOEmail(buscar, pageable);
    //         model.addAttribute("buscar", buscar);
    //     } else {
    //         clientes = clienteService.obtenerTodos(pageable);
    //     }
        
    //     model.addAttribute("clientes", clientes);
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", clientes.getTotalPages());
    //     model.addAttribute("totalElements", clientes.getTotalElements());
    //     model.addAttribute("sort", sort);
    //     model.addAttribute("direction", direction);
        
    //     return "clientes/lista";
    // }
}