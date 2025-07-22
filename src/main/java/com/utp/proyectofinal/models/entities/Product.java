package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Product para PostgreSQL
 * 
 * PATRONES IMPLEMENTADOS:
 * - ENTITY: Patrón Entity de JPA para mapeo objeto-relacional
 * - BUILDER: Patrón Builder con Lombok
 * - VALIDATION: Patrón de validación con Bean Validation
 * - VALUE OBJECT: BigDecimal para representar precios con precisión
 * 
 * @author Julio Pariona
 */
@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_product_name", columnList = "producto"),
    @Index(name = "idx_product_category", columnList = "id_categoria"),
    @Index(name = "idx_product_price", columnList = "precio")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "producto", nullable = false, length = 100, unique = true)
    private String name;

    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Category category;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String description;

    @Size(max = 255, message = "La URL de imagen no puede exceder 255 caracteres")
    @Column(name = "imagen", length = 255)
    private String imageUrl;

    // Campos de auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Estado del producto
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "is_featured", nullable = false)
    private Boolean featured = false;

    // Campos para SEO y marketing
    @Size(max = 150, message = "El slug no puede exceder 150 caracteres")
    @Column(name = "slug", length = 150, unique = true)
    private String slug;

    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    // Campos para integración con MongoDB (reviews, analytics, etc.)
    @Column(name = "mongo_product_id", length = 24)
    private String mongoProductId;

    /**
     * Método para verificar disponibilidad
     */
    public boolean isAvailable() {
        return active != null && active && 
               stock != null && stock > 0 && 
               deletedAt == null;
    }

    /**
     * Método para verificar si tiene stock suficiente
     */
    public boolean hasStock(int requestedQuantity) {
        return stock != null && stock >= requestedQuantity;
    }

    /**
     * Método para reducir stock
     */
    public void reduceStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalArgumentException("Stock insuficiente. Stock actual: " + stock + ", solicitado: " + quantity);
        }
        this.stock -= quantity;
    }

    /**
     * Método para aumentar stock
     */
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        this.stock += quantity;
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
     * Método para obtener precio formateado
     */
    public String getFormattedPrice() {
        return "S/ " + price.toString();
    }

    /**
     * toString personalizado para logging
     */
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", active=" + active +
                ", categoryId=" + (category != null ? category.getId() : null) +
                '}';
    }
}