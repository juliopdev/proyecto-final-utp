package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comprobantes")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Comprobante extends BaseEntity {

    @NotBlank(message = "El tipo de comprobante es obligatorio")
    @Size(max = 50, message = "El tipo de comprobante no puede exceder los 50 caracteres")
    @Column(name = "tipo_comprobante", unique = true, nullable = false, length = 50)
    private String tipoComprobante;

    public Comprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }
}