package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Documento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO {
    
    private String id;
    
    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 50, message = "El tipo de documento no puede exceder los 50 caracteres")
    private String tipoDocumento;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public DocumentoDTO(Documento documento) {
        this.id = documento.getId() != null ? documento.getId().toString() : null;
        this.tipoDocumento = documento.getTipoDocumento();
        this.fechaCreacion = documento.getFechaCreacion();
        this.fechaModificacion = documento.getFechaModificacion();
    }
    
    public Documento toEntity() {
        return new Documento(this.tipoDocumento);
    }
}