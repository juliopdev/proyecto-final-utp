package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolDTO {
    
    private String id;
    
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre del rol no puede exceder los 50 caracteres")
    private String nombreRol;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public RolDTO(Rol rol) {
        this.id = rol.getId() != null ? rol.getId().toString() : null;
        this.nombreRol = rol.getNombreRol();
        this.fechaCreacion = rol.getFechaCreacion();
        this.fechaModificacion = rol.getFechaModificacion();
    }
    
    public Rol toEntity() {
        return new Rol(this.nombreRol);
    }
}