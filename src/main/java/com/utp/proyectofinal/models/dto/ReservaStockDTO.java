package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.ReservaStock;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaStockDTO {
    
    private String id;
    
    @NotNull(message = "La cantidad reservada es obligatoria")
    @Min(value = 1, message = "La cantidad reservada debe ser mayor a 0")
    private Integer cantidadReservada;
    
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(activa|confirmada|liberada|expirada)$", message = "Estado de reserva inválido")
    private String estado;
    
    @NotNull(message = "La fecha de expiración es obligatoria")
    @Future(message = "La fecha de expiración debe ser en el futuro")
    private LocalDateTime fechaExpiracion;
    
    private ProductoDTO producto;
    private ClienteDTO cliente;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    // Campos calculados
    private boolean expirada;
    private boolean activa;
    
    public ReservaStockDTO(ReservaStock reserva) {
        this.id = reserva.getId() != null ? reserva.getId().toString() : null;
        this.cantidadReservada = reserva.getCantidadReservada();
        this.estado = reserva.getEstado();
        this.fechaExpiracion = reserva.getFechaExpiracion();
        this.fechaCreacion = reserva.getFechaCreacion();
        this.fechaModificacion = reserva.getFechaModificacion();
        
        // Campos calculados
        this.expirada = reserva.isExpirada();
        this.activa = reserva.isActiva();
        
        if (reserva.getProducto() != null) {
            this.producto = new ProductoDTO(reserva.getProducto(), true);
        }
        
        if (reserva.getCliente() != null) {
            this.cliente = new ClienteDTO(reserva.getCliente(), false);
        }
    }
    
    public ReservaStock toEntity() {
        ReservaStock reserva = new ReservaStock();
        reserva.setCantidadReservada(this.cantidadReservada);
        reserva.setEstado(this.estado != null ? this.estado : "activa");
        reserva.setFechaExpiracion(this.fechaExpiracion);
        return reserva;
    }
}