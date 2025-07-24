package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Pedido;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    
    private String id;
    
    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.01", message = "El total debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El total debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal total;
    
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(pendiente|confirmado|en_preparacion|en_camino|entregado|cancelado)$", 
             message = "Estado inválido")
    private String estado;
    
    @NotBlank(message = "El tipo de entrega es obligatorio")
    @Pattern(regexp = "^(delivery|recojo|mesa)$", message = "Tipo de entrega inválido")
    private String tipoEntrega;
    
    private String direccionEntrega;
    
    @Min(value = 1, message = "El tiempo estimado debe ser mayor a 0")
    private Integer tiempoEstimado;
    
    @Size(max = 30, message = "El método de pago no puede exceder los 30 caracteres")
    private String metodoPago;
    
    private ClienteDTO cliente;
    private List<DetallePedidoDTO> detalles;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    public PedidoDTO(Pedido pedido) {
        this.id = pedido.getId() != null ? pedido.getId().toString() : null;
        this.total = pedido.getTotal();
        this.estado = pedido.getEstado();
        this.tipoEntrega = pedido.getTipoEntrega();
        this.direccionEntrega = pedido.getDireccionEntrega();
        this.tiempoEstimado = pedido.getTiempoEstimado();
        this.metodoPago = pedido.getMetodoPago();
        this.fechaCreacion = pedido.getFechaCreacion();
        this.fechaModificacion = pedido.getFechaModificacion();
        
        if (pedido.getCliente() != null) {
            this.cliente = new ClienteDTO(pedido.getCliente(), false); // Sin relaciones para evitar ciclos
        }
        
        if (pedido.getDetalles() != null) {
            this.detalles = pedido.getDetalles().stream()
                .map(DetallePedidoDTO::new)
                .collect(Collectors.toList());
        }
    }
    
    public PedidoDTO(Pedido pedido, boolean includeRelations) {
        this.id = pedido.getId() != null ? pedido.getId().toString() : null;
        this.total = pedido.getTotal();
        this.estado = pedido.getEstado();
        this.tipoEntrega = pedido.getTipoEntrega();
        this.direccionEntrega = pedido.getDireccionEntrega();
        this.tiempoEstimado = pedido.getTiempoEstimado();
        this.metodoPago = pedido.getMetodoPago();
        this.fechaCreacion = pedido.getFechaCreacion();
        this.fechaModificacion = pedido.getFechaModificacion();
        
        if (includeRelations) {
            if (pedido.getCliente() != null) {
                this.cliente = new ClienteDTO(pedido.getCliente(), false);
            }
            
            if (pedido.getDetalles() != null) {
                this.detalles = pedido.getDetalles().stream()
                    .map(DetallePedidoDTO::new)
                    .collect(Collectors.toList());
            }
        }
    }
    
    public Pedido toEntity() {
        Pedido pedido = new Pedido();
        pedido.setTotal(this.total);
        pedido.setEstado(this.estado != null ? this.estado : "pendiente");
        pedido.setTipoEntrega(this.tipoEntrega != null ? this.tipoEntrega : "delivery");
        pedido.setDireccionEntrega(this.direccionEntrega);
        pedido.setTiempoEstimado(this.tiempoEstimado != null ? this.tiempoEstimado : 30);
        pedido.setMetodoPago(this.metodoPago);
        return pedido;
    }
}