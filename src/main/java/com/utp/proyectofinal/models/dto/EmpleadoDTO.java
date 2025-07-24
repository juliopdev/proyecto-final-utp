package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Empleado;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoDTO {
    
    private String id;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private String idUsuario; // String para el frontend
    
    private ClienteDTO usuario;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public EmpleadoDTO(Empleado empleado) {
        this.id = empleado.getId() != null ? empleado.getId().toString() : null;
        this.idUsuario = empleado.getIdUsuario() != null ? empleado.getIdUsuario().toString() : null;
        this.fechaCreacion = empleado.getFechaCreacion();
        this.fechaModificacion = empleado.getFechaModificacion();
        
        if (empleado.getUsuario() != null) {
            this.usuario = new ClienteDTO(empleado.getUsuario(), true);
        }
    }
    
    public EmpleadoDTO(Empleado empleado, boolean includeUsuario) {
        this.id = empleado.getId() != null ? empleado.getId().toString() : null;
        this.idUsuario = empleado.getIdUsuario() != null ? empleado.getIdUsuario().toString() : null;
        this.fechaCreacion = empleado.getFechaCreacion();
        this.fechaModificacion = empleado.getFechaModificacion();
        
        if (includeUsuario && empleado.getUsuario() != null) {
            this.usuario = new ClienteDTO(empleado.getUsuario(), true);
        }
    }
    
    public Empleado toEntity() {
        Empleado empleado = new Empleado();
        if (this.idUsuario != null) {
            empleado.setIdUsuario(UUID.fromString(this.idUsuario));
        }
        return empleado;
    }
}