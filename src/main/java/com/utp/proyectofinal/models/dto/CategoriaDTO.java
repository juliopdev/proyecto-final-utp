package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {
    
    private String id;
    
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String nombre;
    
    @Size(max = 10, message = "El icono no puede exceder los 10 caracteres")
    private String icono;
    
    private String descripcion;
    
    @Size(max = 255, message = "La URL de imagen no puede exceder los 255 caracteres")
    private String imagenUrl;
    
    private List<ProductoDTO> productos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public CategoriaDTO(Categoria categoria) {
        this.id = categoria.getId() != null ? categoria.getId().toString() : null;
        this.nombre = categoria.getNombre();
        this.icono = categoria.getIcono();
        this.descripcion = categoria.getDescripcion();
        this.imagenUrl = categoria.getImagenUrl();
        this.fechaCreacion = categoria.getFechaCreacion();
        this.fechaModificacion = categoria.getFechaModificacion();
        
        if (categoria.getProductos() != null) {
            this.productos = categoria.getProductos().stream()
                .map(producto -> new ProductoDTO(producto, false)) // Sin relaciones para evitar ciclos
                .collect(Collectors.toList());
        }
    }
    
    public CategoriaDTO(Categoria categoria, boolean includeProducts) {
        this.id = categoria.getId() != null ? categoria.getId().toString() : null;
        this.nombre = categoria.getNombre();
        this.icono = categoria.getIcono();
        this.descripcion = categoria.getDescripcion();
        this.imagenUrl = categoria.getImagenUrl();
        this.fechaCreacion = categoria.getFechaCreacion();
        this.fechaModificacion = categoria.getFechaModificacion();
        
        if (includeProducts && categoria.getProductos() != null) {
            this.productos = categoria.getProductos().stream()
                .map(producto -> new ProductoDTO(producto, false))
                .collect(Collectors.toList());
        }
    }
    
    public Categoria toEntity() {
        Categoria categoria = new Categoria();
        categoria.setNombre(this.nombre);
        categoria.setIcono(this.icono);
        categoria.setDescripcion(this.descripcion);
        categoria.setImagenUrl(this.imagenUrl);
        return categoria;
    }
}