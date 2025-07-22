package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Category para PostgreSQL
 * 
 * PATRONES IMPLEMENTADOS:
 * - ENTITY: Patrón Entity de JPA para mapeo objeto-relacional
 * - BUILDER: Patrón Builder con Lombok
 * - VALIDATION: Patrón de validación con Bean Validation
 * - COMPOSITE: Relación uno-a-muchos con productos
 * 
 * @author Julio Pariona
 */
@Entity
@Table(name = "categorias", indexes = {
    @Index(name = "idx_category_name", columnList = "categoria")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(name = "categoria", nullable = false, length = 50, unique = true)
    private String name;

    @Size(max = 255, message = "La URL del icono no puede exceder 255 caracteres")
    @Column(name = "icono", length = 255)
    private String iconUrl;

    // Campos adicionales para mejorar la funcionalidad
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String description;

    @Size(max = 100, message = "El slug no puede exceder 100 caracteres")
    @Column(name = "slug", length = 100, unique = true)
    private String slug;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // Campos de auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relación con productos (PATRÓN COMPOSITE)
    @Builder.Default
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    /**
     * Método para verificar si la categoría está activa
     */
    public boolean isActive() {
        return active != null && active && deletedAt == null;
    }

    /**
     * Método para generar slug automáticamente
     */
    @PrePersist
    @PreUpdate
    public void generateSlug() {
        if (this.name != null && (this.slug == null || this.slug.isEmpty())) {
            this.slug = this.name.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .trim();
        }
    }

    /**
     * Método para soft delete
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.active = false;
    }

    /**
     * Método para obtener productos activos
     */
    public List<Product> getActiveProducts() {
        return products.stream()
                .filter(Product::isAvailable)
                .toList();
    }

    /**
     * Método para contar productos activos
     */
    public long getActiveProductCount() {
        return products.stream()
                .filter(Product::isAvailable)
                .count();
    }

    /**
     * Método para agregar producto (PATRÓN COMPOSITE)
     */
    public void addProduct(Product product) {
        if (product != null) {
            products.add(product);
            product.setCategory(this);
        }
    }

    /**
     * Método para remover producto (PATRÓN COMPOSITE)
     */
    public void removeProduct(Product product) {
        if (product != null) {
            products.remove(product);
            product.setCategory(null);
        }
    }

    /**
     * toString personalizado para logging
     */
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", active=" + active +
                ", productCount=" + (products != null ? products.size() : 0) +
                '}';
    }
}