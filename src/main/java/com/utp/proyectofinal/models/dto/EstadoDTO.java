package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Estado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoDTO {
    
    private String id;
    
    @NotBlank(message = "El nombre del estado es obligatorio")
    @Size(max = 50, message = "El nombre del estado no puede exceder los 50 caracteres")
    private String nombreEstado;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public EstadoDTO(Estado estado) {
        this.id = estado.getId() != null ? estado.getId().toString() : null;
        this.nombreEstado = estado.getNombreEstado();
        this.fechaCreacion = estado.getFechaCreacion();
        this.fechaModificacion = estado.getFechaModificacion();
    }
    
    public Estado toEntity() {
        return new Estado(this.nombreEstado);
    }
}