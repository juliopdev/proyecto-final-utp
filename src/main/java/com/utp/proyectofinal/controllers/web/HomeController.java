package com.utp.proyectofinal.controllers.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.utp.proyectofinal.repositories.mongodb.ComentarioRepository;
import com.utp.proyectofinal.repositories.postgresql.CategoriaRepository;
import com.utp.proyectofinal.repositories.postgresql.ProductoRepository;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final CategoriaRepository categoriaRepository;
    private final ComentarioRepository comentarioRepository;
    private final ProductoRepository productoRepository;

    @GetMapping("/")
    public String home(Model model) {
        try {
            model.addAttribute("titulo", "Inicio");
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("comentarios", comentarioRepository.findAll());
            
            return "home/index";
            
        } catch (Exception e) {
            log.error("Error al cargar la página de inicio: ", e);
            model.addAttribute("error", "Error al cargar la página");
            return "error/500";
        }
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        try {
            model.addAttribute("titulo", "Nosotros");
            return "home/nosotros";
            
        } catch (Exception e) {
            log.error("Error al cargar página nosotros: ", e);
            model.addAttribute("error", "Error al cargar la página");
            return "error/500";
        }
    }

    @GetMapping("/sobre-nosotros")
    public String sobreNosotros(Model model) {
        try {
            model.addAttribute("titulo", "Sobre Nosotros");
            return "home/sobre-nosotros";
            
        } catch (Exception e) {
            log.error("Error al cargar página sobre nosotros: ", e);
            model.addAttribute("error", "Error al cargar la página");
            return "error/500";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("titulo", "Dashboard");
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("productos", productoRepository.findAll());
            
            log.info("Dashboard cargado exitosamente");
            return "admin/admin";
            
        } catch (Exception e) {
            log.error("Error al cargar dashboard: ", e);
            model.addAttribute("error", "Error al cargar el dashboard");
            return "error/500";
        }
    }
}