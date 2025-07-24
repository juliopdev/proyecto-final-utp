package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estados")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Estado extends BaseEntity {

    @NotBlank(message = "El nombre del estado es obligatorio")
    @Size(max = 50, message = "El nombre del estado no puede exceder los 50 caracteres")
    @Column(name = "nombre_estado", unique = true, nullable = false, length = 50)
    private String nombreEstado;

    public Estado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }
}