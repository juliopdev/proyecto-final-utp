package com.utp.proyectofinal.controllers.web;

import com.utp.proyectofinal.models.entities.Category;
import com.utp.proyectofinal.models.entities.Product;
import com.utp.proyectofinal.services.interfaces.ProductService;
import com.utp.proyectofinal.services.interfaces.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para gestión y visualización de productos
 * 
 * PATRONES IMPLEMENTADOS:
 * - MVC: Patrón Model-View-Controller para productos
 * - COMMAND: Cada método representa un comando específico
 * - STRATEGY: Diferentes estrategias de filtrado y ordenamiento
 * - OBSERVER: Registra eventos de visualización de productos
 * - FACADE: Coordina servicios para vistas de productos
 * 
 * @author Julio Pariona
 */
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final LogService logService;

    /**
     * PATRÓN TEMPLATE METHOD: Template para listado de productos
     */
    @GetMapping
    public String listProducts(
            @RequestParam(value = "category", required = false) String categorySlug,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "order", defaultValue = "asc") String order,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model,
            HttpServletRequest request) {

        log.debug("Listando productos - Categoría: {}, Página: {}", categorySlug, page);

        try {
            // PATRÓN STRATEGY: Diferentes estrategias de ordenamiento
            Sort.Direction direction = "desc".equalsIgnoreCase(order) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            
            Sort sortBy = Sort.by(direction, getSortField(sort));
            Pageable pageable = PageRequest.of(page, size, sortBy);

            Page<Product> products;
            Category selectedCategory = null;

            if (categorySlug != null && !categorySlug.trim().isEmpty()) {
                // PATRÓN COMMAND: Filtrar por categoría
                selectedCategory = productService.getCategoryBySlug(categorySlug);
                if (selectedCategory != null) {
                    products = productService.getProductsByCategory(selectedCategory.getId(), pageable);
                    log.debug("Productos filtrados por categoría '{}': {} resultados", 
                             selectedCategory.getName(), products.getTotalElements());
                } else {
                    log.warn("Categoría no encontrada: {}", categorySlug);
                    model.addAttribute("errorMessage", "Categoría no encontrada");
                    products = productService.getAllActiveProducts(pageable);
                }
            } else {
                // PATRÓN COMMAND: Obtener todos los productos
                products = productService.getAllActiveProducts(pageable);
            }

            // PATRÓN FACADE: Obtener datos adicionales para la vista
            List<Category> categories = productService.getAllActiveCategories();
            
            // Preparar modelo para la vista
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("selectedCategory", selectedCategory);
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentOrder", order);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("totalElements", products.getTotalElements());
            model.addAttribute("isFirstPage", products.isFirst());
            model.addAttribute("isLastPage", products.isLast());

            // Configurar título y descripción de página
            String pageTitle = selectedCategory != null ? 
                "Productos - " + selectedCategory.getName() + " - Proyecto Final UTP" :
                "Todos los Productos - Proyecto Final UTP";
            
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("pageDescription", "Descubre nuestra amplia gama de productos");

            // PATRÓN OBSERVER: Registrar visualización de listado
            String userEmail = getCurrentUserEmail();
            logService.logProductListing(categorySlug, products.getTotalElements(), 
                                       userEmail, getClientIP(request));

            log.debug("Listado de productos cargado exitosamente: {} productos", 
                     products.getTotalElements());
            
            return "products/list";

        } catch (Exception e) {
            log.error("Error cargando listado de productos: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error cargando los productos");
            model.addAttribute("pageTitle", "Error - Productos");
            return "error/500";
        }
    }

    /**
     * PATRÓN COMMAND: Comando para ver detalle de producto
     */
    @GetMapping("/{slug}")
    public String viewProduct(
            @PathVariable("slug") String slug,
            Model model,
            HttpServletRequest request) {

        log.debug("Visualizando producto con slug: {}", slug);

        try {
            // PATRÓN COMMAND: Obtener producto por slug
            Optional<Product> productOpt = productService.getProductBySlug(slug);
            
            if (productOpt.isEmpty()) {
                log.warn("Producto no encontrado con slug: {}", slug);
                model.addAttribute("errorMessage", "Producto no encontrado");
                model.addAttribute("pageTitle", "Producto No Encontrado");
                return "error/404";
            }

            Product product = productOpt.get();
            
            // PATRÓN STRATEGY: Verificar disponibilidad
            if (!product.isAvailable()) {
                log.warn("Producto no disponible: {}", slug);
                model.addAttribute("warningMessage", "Este producto no está disponible actualmente");
            }

            // PATRÓN FACADE: Obtener datos relacionados
            List<Product> relatedProducts = productService
                .getRelatedProducts(product.getCategory().getId(), product.getId(), 4);
            
            List<Product> similarProducts = productService
                .getSimilarProducts(product.getId(), 4);

            // Preparar modelo para la vista
            model.addAttribute("product", product);
            model.addAttribute("relatedProducts", relatedProducts);
            model.addAttribute("similarProducts", similarProducts);
            model.addAttribute("pageTitle", product.getName() + " - Proyecto Final UTP");
            model.addAttribute("pageDescription", product.getDescription());
            model.addAttribute("category", product.getCategory());
            model.addAttribute("isAvailable", product.isAvailable());
            model.addAttribute("inStock", product.hasStock(1));

            // PATRÓN OBSERVER: Registrar visualización de producto
            String userEmail = getCurrentUserEmail();
            logService.logProductViewed(product.getName(), userEmail, getClientIP(request));

            log.info("Producto visualizado: '{}' por usuario: {}", product.getName(), userEmail);
            
            return "products/detail";

        } catch (Exception e) {
            log.error("Error visualizando producto '{}': {}", slug, e.getMessage());
            model.addAttribute("errorMessage", "Error cargando el producto");
            model.addAttribute("pageTitle", "Error - Producto");
            return "error/500";
        }
    }

    /**
     * PATRÓN STRATEGY: Endpoint para productos por categoría (AJAX)
     */
    @GetMapping("/category/{categorySlug}")
    public String productsByCategory(
            @PathVariable("categorySlug") String categorySlug,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model,
            HttpServletRequest request) {

        log.debug("Cargando productos por categoría: {}", categorySlug);

        // Redirigir al método principal con parámetro de categoría
        return "redirect:/products?category=" + categorySlug + "&page=" + page + "&size=" + size;
    }

    /**
     * PATRÓN COMMAND: API para búsqueda rápida de productos (AJAX)
     */
    @GetMapping("/api/quick-search")
    @ResponseBody
    public List<Product> quickSearch(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "5") int limit,
            HttpServletRequest request) {
        
        log.debug("Búsqueda rápida: '{}', límite: {}", query, limit);

        try {
            List<Product> results = productService.quickSearch(query, limit);
            
            // PATRÓN OBSERVER: Registrar búsqueda rápida
            String userEmail = getCurrentUserEmail();
            logService.logQuickSearch(query, results.size(), userEmail, getClientIP(request));

            log.debug("Búsqueda rápida completada: {} resultados para '{}'", results.size(), query);
            return results;

        } catch (Exception e) {
            log.error("Error en búsqueda rápida: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * PATRÓN STRATEGY: API para verificar stock de producto (AJAX)
     */
    @GetMapping("/api/{productId}/stock")
    @ResponseBody
    public Map<String, Object> checkStock(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        
        log.debug("Verificando stock para producto ID: {}, cantidad: {}", productId, quantity);

        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            Map<String, Object> response = new HashMap<>();
            
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                boolean hasStock = product.hasStock(quantity);
                boolean isAvailable = product.isAvailable();
                
                response.put("hasStock", hasStock);
                response.put("isAvailable", isAvailable);
                response.put("currentStock", product.getStock());
                response.put("productName", product.getName());
                response.put("maxQuantity", product.getStock());
                
                if (!hasStock) {
                    response.put("message", "Stock insuficiente. Stock disponible: " + product.getStock());
                } else if (!isAvailable) {
                    response.put("message", "Producto no disponible actualmente");
                } else {
                    response.put("message", "Producto disponible");
                }
            } else {
                response.put("hasStock", false);
                response.put("isAvailable", false);
                response.put("message", "Producto no encontrado");
            }
            
            return response;

        } catch (Exception e) {
            log.error("Error verificando stock: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("hasStock", false);
            errorResponse.put("isAvailable", false);
            errorResponse.put("message", "Error verificando disponibilidad");
            return errorResponse;
        }
    }

    /**
     * PATRÓN COMMAND: API para obtener productos relacionados (AJAX)
     */
    @GetMapping("/api/{productId}/related")
    @ResponseBody
    public List<Product> getRelatedProducts(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "limit", defaultValue = "4") int limit) {
        
        log.debug("Obteniendo productos relacionados para ID: {}", productId);

        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                return productService.getRelatedProducts(product.getCategory().getId(), productId, limit);
            }
            return List.of();

        } catch (Exception e) {
            log.error("Error obteniendo productos relacionados: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * PATRÓN STRATEGY: Endpoint para filtros avanzados
     */
    @GetMapping("/filter")
    public String filterProducts(
            @RequestParam(value = "category", required = false) String categorySlug,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "inStock", required = false, defaultValue = "false") boolean inStock,
            @RequestParam(value = "featured", required = false, defaultValue = "false") boolean featured,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "order", defaultValue = "asc") String order,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model,
            HttpServletRequest request) {

        log.debug("Aplicando filtros avanzados - Categoría: {}, Precio: {}-{}, En stock: {}", 
                 categorySlug, minPrice, maxPrice, inStock);

        try {
            // PATRÓN STRATEGY: Construir filtros
            Map<String, Object> filters = new HashMap<>();
            if (categorySlug != null) filters.put("category", categorySlug);
            if (minPrice != null) filters.put("minPrice", minPrice);
            if (maxPrice != null) filters.put("maxPrice", maxPrice);
            if (inStock) filters.put("inStock", true);
            if (featured) filters.put("featured", true);

            Sort.Direction direction = "desc".equalsIgnoreCase(order) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sortBy = Sort.by(direction, getSortField(sort));
            Pageable pageable = PageRequest.of(page, size, sortBy);

            // PATRÓN COMMAND: Aplicar filtros
            Page<Product> products = productService.filterProducts(filters, pageable);
            List<Category> categories = productService.getAllActiveCategories();

            // Preparar modelo
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("appliedFilters", filters);
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentOrder", order);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("totalElements", products.getTotalElements());
            model.addAttribute("pageTitle", "Productos Filtrados - Proyecto Final UTP");

            // PATRÓN OBSERVER: Registrar uso de filtros
            String userEmail = getCurrentUserEmail();
            logService.logProductFilter(filters.toString(), products.getTotalElements(), 
                                      userEmail, getClientIP(request));

            return "products/list";

        } catch (Exception e) {
            log.error("Error aplicando filtros: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error aplicando filtros");
            return "products/list";
        }
    }

    // ===== MÉTODOS UTILITARIOS =====

    /**
     * PATRÓN STRATEGY: Mapear parámetros de ordenamiento a campos de entidad
     */
    private String getSortField(String sort) {
        switch (sort.toLowerCase()) {
            case "price":
                return "price";
            case "name":
                return "name";
            case "created":
                return "createdAt";
            case "stock":
                return "stock";
            case "category":
                return "category.name";
            default:
                return "name";
        }
    }

    /**
     * Obtener email del usuario actual
     */
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "anonymous";
    }

    /**
     * Obtener IP del cliente
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    /**
     * PATRÓN STRATEGY: Manejo de excepciones específicas del controlador
     */
    @ExceptionHandler(Exception.class)
    public String handleProductException(Exception e, Model model, HttpServletRequest request) {
        log.error("Error en ProductController: {}", e.getMessage(), e);
        
        // PATRÓN OBSERVER: Registrar error
        String userEmail = getCurrentUserEmail();
        logService.logSystemError("ProductController: " + e.getMessage(), 
                                 e.getStackTrace().toString(), "WEB");

        model.addAttribute("pageTitle", "Error - Productos");
        model.addAttribute("errorMessage", "Ha ocurrido un error cargando los productos");
        
        return "error/500";
    }
}