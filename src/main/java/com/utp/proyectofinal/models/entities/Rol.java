package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Rol extends BaseEntity {

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre del rol no puede exceder los 50 caracteres")
    @Column(name = "nombre_rol", unique = true, nullable = false, length = 50)
    private String nombreRol;

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private List<Cliente> clientes;
    public Rol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public List<Empleado> getEmpleados() {
        if (clientes == null) {
            return List.of();
        }
        
        return clientes.stream()
            .filter(cliente -> cliente.getEmpleados() != null)
            .flatMap(cliente -> cliente.getEmpleados().stream())
            .toList();
    }
}