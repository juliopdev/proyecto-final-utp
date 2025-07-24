package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.MetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoDTO {
    
    private String id;
    
    @NotBlank(message = "El nombre del método de pago es obligatorio")
    @Size(max = 50, message = "El nombre del método no puede exceder los 50 caracteres")
    private String nombreMetodo;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public MetodoPagoDTO(MetodoPago metodo) {
        this.id = metodo.getId() != null ? metodo.getId().toString() : null;
        this.nombreMetodo = metodo.getNombreMetodo();
        this.fechaCreacion = metodo.getFechaCreacion();
        this.fechaModificacion = metodo.getFechaModificacion();
    }
    
    public MetodoPago toEntity() {
        return new MetodoPago(this.nombreMetodo);
    }
}