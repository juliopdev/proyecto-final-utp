package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Role para PostgreSQL
 * 
 * PATRONES IMPLEMENTADOS:
 * - ENTITY: Patrón Entity de JPA para mapeo objeto-relacional
 * - BUILDER: Patrón Builder con Lombok
 * - ENUM: Representación de roles como enumeración para type safety
 * 
 * @author Julio Pariona
 */
@Entity
@Table(name = "rol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 100)
    private RoleName name;

    /**
     * Enumeración de roles disponibles en el sistema
     */
    public enum RoleName {
        ADMIN("Admin"),
        USER("User"),
        REPARTIDOR("Repartidor"),
        VENDEDOR("Vendedor");

        private final String displayName;

        RoleName(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * PATRÓN FACTORY: Factory method para crear desde string
         */
        public static RoleName fromString(String role) {
            for (RoleName r : RoleName.values()) {
                if (r.getDisplayName().equalsIgnoreCase(role) || 
                    r.name().equalsIgnoreCase(role)) {
                    return r;
                }
            }
            throw new IllegalArgumentException("Rol no válido: " + role);
        }
    }

    /**
     * Constructor de conveniencia
     */
    public Role(RoleName name) {
        this.name = name;
    }

    /**
     * Método para verificar si es admin
     */
    public boolean isAdmin() {
        return RoleName.ADMIN.equals(this.name);
    }

    /**
     * Método para verificar si es usuario regular
     */
    public boolean isUser() {
        return RoleName.USER.equals(this.name);
    }

    /**
     * Método para verificar si es repartidor
     */
    public boolean isRepartidor() {
        return RoleName.REPARTIDOR.equals(this.name);
    }

    /**
     * Método para verificar si es vendedor
     */
    public boolean isVendedor() {
        return RoleName.VENDEDOR.equals(this.name);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name=" + name +
                '}';
    }
}