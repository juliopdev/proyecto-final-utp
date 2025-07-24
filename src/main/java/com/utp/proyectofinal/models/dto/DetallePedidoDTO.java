package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.DetallePedido;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoDTO {
    
    private String id;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
    
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
    @Digits(integer = 6, fraction = 2, message = "El precio debe tener máximo 6 dígitos enteros y 2 decimales")
    private BigDecimal precioUnitario;
    
    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.01", message = "El subtotal debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El subtotal debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal subtotal;
    
    private ProductoDTO producto;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public DetallePedidoDTO(DetallePedido detalle) {
        this.id = detalle.getId() != null ? detalle.getId().toString() : null;
        this.cantidad = detalle.getCantidad();
        this.precioUnitario = detalle.getPrecioUnitario();
        this.subtotal = detalle.getSubtotal();
        this.fechaCreacion = detalle.getFechaCreacion();
        this.fechaModificacion = detalle.getFechaModificacion();
        
        if (detalle.getProducto() != null) {
            this.producto = new ProductoDTO(detalle.getProducto(), true); // Con categoría
        }
    }
    
    public DetallePedido toEntity() {
        DetallePedido detalle = new DetallePedido();
        detalle.setCantidad(this.cantidad);
        detalle.setPrecioUnitario(this.precioUnitario);
        detalle.setSubtotal(this.subtotal);
        return detalle;
    }
}