package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_clientes_email", columnList = "email"),
    @Index(name = "idx_clientes_rol", columnList = "id_rol")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cliente extends BaseEntity {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 255, message = "La contraseña no puede exceder los 255 caracteres")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "El formato del teléfono no es válido")
    @Size(max = 15, message = "El teléfono no puede exceder los 15 caracteres")
    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false, foreignKey = @ForeignKey(name = "fk_cliente_rol"))
    private Rol rol;

    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos")
    @Column(name = "dni", length = 8)
    private String dni;

    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener exactamente 11 dígitos")
    @Column(name = "ruc", length = 11)
    private String ruc;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pedido> pedidos;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReservaStock> reservasStock;

    // AGREGAMOS la relación con Empleado
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Empleado> empleados;

    @PrePersist
    @PreUpdate
    private void validateDocuments() {
        if ((dni == null || dni.trim().isEmpty()) && (ruc == null || ruc.trim().isEmpty())) {
            throw new IllegalArgumentException("Debe proporcionar al menos un DNI o RUC");
        }
    }
}