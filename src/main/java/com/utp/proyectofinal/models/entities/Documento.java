package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documentos")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Documento extends BaseEntity {

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 50, message = "El tipo de documento no puede exceder los 50 caracteres")
    @Column(name = "tipo_documento", unique = true, nullable = false, length = 50)
    private String tipoDocumento;

    public Documento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
}