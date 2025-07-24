package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Comprobante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteDTO {
    
    private String id;
    
    @NotBlank(message = "El tipo de comprobante es obligatorio")
    @Size(max = 50, message = "El tipo de comprobante no puede exceder los 50 caracteres")
    private String tipoComprobante;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public ComprobanteDTO(Comprobante comprobante) {
        this.id = comprobante.getId() != null ? comprobante.getId().toString() : null;
        this.tipoComprobante = comprobante.getTipoComprobante();
        this.fechaCreacion = comprobante.getFechaCreacion();
        this.fechaModificacion = comprobante.getFechaModificacion();
    }
    
    public Comprobante toEntity() {
        return new Comprobante(this.tipoComprobante);
    }
}