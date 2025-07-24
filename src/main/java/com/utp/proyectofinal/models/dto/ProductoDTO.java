package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Producto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    
    private String id;
    
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;
    
    private String descripcion;
    
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 6, fraction = 2, message = "El precio debe tener máximo 6 dígitos enteros y 2 decimales")
    private BigDecimal precioUnitario;
    
    @Size(max = 255, message = "La URL de imagen no puede exceder los 255 caracteres")
    private String imagenUrl;
    
    private CategoriaDTO categoria;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public ProductoDTO(Producto producto) {
        this.id = producto.getId() != null ? producto.getId().toString() : null;
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.stock = producto.getStock();
        this.precioUnitario = producto.getPrecioUnitario();
        this.imagenUrl = producto.getImagenUrl();
        this.fechaCreacion = producto.getFechaCreacion();
        this.fechaModificacion = producto.getFechaModificacion();
        
        if (producto.getCategoria() != null) {
            this.categoria = new CategoriaDTO(producto.getCategoria(), false); // Sin productos para evitar ciclos
        }
    }
    
    public ProductoDTO(Producto producto, boolean includeCategory) {
        this.id = producto.getId() != null ? producto.getId().toString() : null;
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.stock = producto.getStock();
        this.precioUnitario = producto.getPrecioUnitario();
        this.imagenUrl = producto.getImagenUrl();
        this.fechaCreacion = producto.getFechaCreacion();
        this.fechaModificacion = producto.getFechaModificacion();
        
        if (includeCategory && producto.getCategoria() != null) {
            this.categoria = new CategoriaDTO(producto.getCategoria(), false);
        }
    }
    
    public Producto toEntity() {
        Producto producto = new Producto();
        producto.setNombre(this.nombre);
        producto.setDescripcion(this.descripcion);
        producto.setStock(this.stock != null ? this.stock : 0);
        producto.setPrecioUnitario(this.precioUnitario);
        producto.setImagenUrl(this.imagenUrl);
        return producto;
    }
}