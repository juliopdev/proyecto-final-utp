package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.documents.Comentario;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDTO {
    
    private String id; // String simple para el frontend
    
    @NotBlank(message = "El ID del cliente es obligatorio")
    private String idCliente;
    
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;
    
    @NotBlank(message = "El comentario es obligatorio")
    @Size(min = 10, max = 500, message = "El comentario debe tener entre 10 y 500 caracteres")
    private String comentario;
    
    @Min(value = 1, message = "La puntuación debe ser al menos 1")
    @Max(value = 5, message = "La puntuación no puede exceder 5")
    private int puntuacion;
    
    private LocalDateTime fechaCreacion;
    
    public ComentarioDTO(Comentario comentario) {
        this.id = comentario.getId() != null ? comentario.getId().toString() : null;
        this.idCliente = comentario.getIdCliente();
        this.nombreCliente = comentario.getNombreCliente();
        this.comentario = comentario.getComentario();
        this.puntuacion = comentario.getPuntuacion();
        this.fechaCreacion = comentario.getFechaCreacion();
    }
    
    public Comentario toDocument() {
        Comentario doc = new Comentario();
        doc.setIdCliente(this.idCliente);
        doc.setNombreCliente(this.nombreCliente);
        doc.setComentario(this.comentario);
        doc.setPuntuacion(this.puntuacion);
        return doc;
    }
}