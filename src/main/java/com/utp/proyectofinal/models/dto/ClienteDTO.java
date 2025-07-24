package com.utp.proyectofinal.models.dto;

import com.utp.proyectofinal.models.entities.Cliente;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    
    private String id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 255, message = "La contraseña no puede exceder los 255 caracteres")
    private String passwordHash;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "El formato del teléfono no es válido")
    @Size(max = 15, message = "El teléfono no puede exceder los 15 caracteres")
    private String telefono;
    
    private String direccion;
    
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;
    
    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener exactamente 11 dígitos")
    private String ruc;
    
    private RolDTO rol;
    private List<PedidoDTO> pedidos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    
    // Constructor desde Entity
    public ClienteDTO(Cliente cliente) {
        this.id = cliente.getId() != null ? cliente.getId().toString() : null;
        this.nombre = cliente.getNombre();
        this.email = cliente.getEmail();
        this.passwordHash = cliente.getPasswordHash();
        this.telefono = cliente.getTelefono();
        this.direccion = cliente.getDireccion();
        this.dni = cliente.getDni();
        this.ruc = cliente.getRuc();
        this.fechaCreacion = cliente.getFechaCreacion();
        this.fechaModificacion = cliente.getFechaModificacion();
        
        if (cliente.getRol() != null) {
            this.rol = new RolDTO(cliente.getRol());
        }
        
        if (cliente.getPedidos() != null) {
            this.pedidos = cliente.getPedidos().stream()
                .map(PedidoDTO::new)
                .collect(Collectors.toList());
        }
    }
    
    // Constructor básico sin relaciones (para evitar lazy loading)
    public ClienteDTO(Cliente cliente, boolean includeRelations) {
        this.id = cliente.getId() != null ? cliente.getId().toString() : null;
        this.nombre = cliente.getNombre();
        this.email = cliente.getEmail();
        this.passwordHash = cliente.getPasswordHash();
        this.telefono = cliente.getTelefono();
        this.direccion = cliente.getDireccion();
        this.dni = cliente.getDni();
        this.ruc = cliente.getRuc();
        this.fechaCreacion = cliente.getFechaCreacion();
        this.fechaModificacion = cliente.getFechaModificacion();
        
        if (includeRelations && cliente.getRol() != null) {
            this.rol = new RolDTO(cliente.getRol());
        }
    }
    
    public Cliente toEntity() {
        Cliente cliente = new Cliente();
        cliente.setNombre(this.nombre);
        cliente.setEmail(this.email);
        cliente.setPasswordHash(this.passwordHash);
        cliente.setTelefono(this.telefono);
        cliente.setDireccion(this.direccion);
        cliente.setDni(this.dni);
        cliente.setRuc(this.ruc);
        return cliente;
    }
}