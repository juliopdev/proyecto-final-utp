package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas_stock", indexes = {
    @Index(name = "idx_reserva_producto", columnList = "id_producto"),
    @Index(name = "idx_reserva_usuario", columnList = "id_usuario"),
    @Index(name = "idx_reserva_estado", columnList = "estado"),
    @Index(name = "idx_reserva_expiracion", columnList = "fecha_expiracion")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReservaStock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false, foreignKey = @ForeignKey(name = "fk_reserva_producto"))
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "fk_reserva_usuario"))
    private Cliente cliente;

    @NotNull(message = "La cantidad reservada es obligatoria")
    @Min(value = 1, message = "La cantidad reservada debe ser mayor a 0")
    @Column(name = "cantidad_reservada", nullable = false)
    private Integer cantidadReservada;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(activa|confirmada|liberada|expirada)$", message = "Estado de reserva inválido")
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "activa";

    @NotNull(message = "La fecha de expiración es obligatoria")
    @Future(message = "La fecha de expiración debe ser en el futuro")
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @PrePersist
    private void setDefaultExpiration() {
        if (fechaExpiracion == null) {
            // Reserva por defecto de 30 minutos
            fechaExpiracion = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Verifica si la reserva ha expirado
     */
    public boolean isExpirada() {
        return LocalDateTime.now().isAfter(fechaExpiracion) && "activa".equals(estado);
    }

    /**
     * Verifica si la reserva está activa y no ha expirado
     */
    public boolean isActiva() {
        return "activa".equals(estado) && !isExpirada();
    }
}