package com.utp.proyectofinal.services.interfaces;

import com.utp.proyectofinal.models.entities.Category;
import com.utp.proyectofinal.models.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface básica para ProductService
 * 
 * PATRONES IMPLEMENTADOS:
 * - STRATEGY: Diferentes estrategias de búsqueda y filtrado
 * - COMMAND: Comandos para operaciones de productos
 * - REPOSITORY: Abstracción de acceso a datos
 * 
 * @author Julio Pariona
 */
public interface ProductService {

    // Operaciones básicas
    Optional<Product> getProductById(Long id);
    Optional<Product> getProductBySlug(String slug);
    List<Product> getFeaturedProducts(int limit);
    List<Product> getRecentProducts(int limit);
    
    // Operaciones con paginación
    Page<Product> getAllActiveProducts(Pageable pageable);
    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);
    Page<Product> searchProducts(String query, String category, Pageable pageable);
    Page<Product> filterProducts(Map<String, Object> filters, Pageable pageable);
    
    // Operaciones de categorías
    List<Category> getAllActiveCategories();
    Category getCategoryBySlug(String slug);
    
    // Operaciones relacionadas
    List<Product> getRelatedProducts(Long categoryId, Long excludeProductId, int limit);
    List<Product> getSimilarProducts(Long productId, int limit);
    
    // Búsquedas especiales
    List<Product> quickSearch(String query, int limit);
    
    // Estadísticas
    long countActiveProducts();
    long countActiveCategories();
    long countFeaturedProducts();
}