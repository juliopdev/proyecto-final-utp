package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "pedidos", indexes = {
    @Index(name = "idx_pedidos_cliente", columnList = "id_cliente"),
    @Index(name = "idx_pedidos_fecha_creacion", columnList = "fecha_creacion"),
    @Index(name = "idx_pedidos_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Pedido extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", foreignKey = @ForeignKey(name = "fk_pedido_cliente"))
    private Cliente cliente;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.01", message = "El total debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El total debe tener máximo 8 dígitos enteros y 2 decimales")
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(pendiente|confirmado|en_preparacion|en_camino|entregado|cancelado)$", 
             message = "Estado inválido")
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "pendiente";

    @NotBlank(message = "El tipo de entrega es obligatorio")
    @Pattern(regexp = "^(delivery|recojo|mesa)$", message = "Tipo de entrega inválido")
    @Column(name = "tipo_entrega", nullable = false, length = 20)
    private String tipoEntrega = "delivery";

    @Column(name = "direccion_entrega", columnDefinition = "TEXT")
    private String direccionEntrega;

    @Min(value = 1, message = "El tiempo estimado debe ser mayor a 0")
    @Column(name = "tiempo_estimado")
    private Integer tiempoEstimado = 30;

    @Size(max = 30, message = "El método de pago no puede exceder los 30 caracteres")
    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePedido> detalles;

    @PrePersist
    @PreUpdate
    private void validateDeliveryAddress() {
        if ("delivery".equals(tipoEntrega)) {
            if (direccionEntrega == null || direccionEntrega.trim().isEmpty()) {
                throw new IllegalArgumentException("La dirección de entrega es obligatoria para delivery");
            }
        }
    }
}