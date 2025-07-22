package com.utp.proyectofinal.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad User para PostgreSQL
 * 
 * PATRONES IMPLEMENTADOS:
 * - BUILDER: Patrón Builder con Lombok para construcción fluida
 * - ENTITY: Patrón Entity de JPA para mapeo objeto-relacional
 * - VALIDATION: Patrón de validación con Bean Validation
 * 
 * Representa los datos principales de usuarios en PostgreSQL
 * Los datos adicionales y logs se almacenan en MongoDB
 * 
 * @author Julio Pariona
 */
@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_user_email", columnList = "correo"),
    @Index(name = "idx_user_phone", columnList = "telefono"),
    @Index(name = "idx_user_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(name = "apellido", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Column(name = "correo", nullable = false, length = 100, unique = true)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "contraseña", nullable = false)
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos")
    @Column(name = "telefono", length = 15)
    private String phone;

    @Column(name = "fecha_nacimiento")
    private LocalDate birthDate;

    @Column(name = "dirección", length = 222)
    private String address;

    @NotNull(message = "El rol es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Role role;

    // Campos de auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Estado de la cuenta
    @Builder.Default
    @Column(name = "is_enabled", nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(name = "is_locked", nullable = false)
    private Boolean locked = false;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean verified = false;

    // Campos para integración con MongoDB
    @Column(name = "mongo_user_id", length = 24)
    private String mongoUserId; // ObjectId de MongoDB como String

    /**
     * PATRÓN ADAPTER: Convierte roles a Set para compatibilidad con Spring Security
     */
    public Set<Role> getRoles() {
        Set<Role> roles = new HashSet<>();
        if (this.role != null) {
            roles.add(this.role);
        }
        return roles;
    }

    /**
     * Métodos de conveniencia para Spring Security
     */
    public boolean isEnabled() {
        return enabled != null && enabled && deletedAt == null;
    }

    public boolean isLocked() {
        return locked != null && locked;
    }

    public boolean isVerified() {
        return verified != null && verified;
    }

    /**
     * Método para obtener el nombre completo
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Método para soft delete
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.enabled = false;
    }

    /**
     * Método para actualizar último login
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * toString personalizado para logging (sin password)
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", enabled=" + enabled +
                ", verified=" + verified +
                ", mongoUserId='" + mongoUserId + '\'' +
                '}';
    }
}