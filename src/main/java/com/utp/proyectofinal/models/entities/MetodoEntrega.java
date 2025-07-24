package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metodos_entrega")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MetodoEntrega extends BaseEntity {

    @NotBlank(message = "El nombre del método de entrega es obligatorio")
    @Size(max = 50, message = "El nombre del método no puede exceder los 50 caracteres")
    @Column(name = "nombre_metodo", unique = true, nullable = false, length = 50)
    private String nombreMetodo;

    public MetodoEntrega(String nombreMetodo) {
        this.nombreMetodo = nombreMetodo;
    }
}