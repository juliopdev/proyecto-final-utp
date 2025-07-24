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
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Categoria extends BaseEntity {

  @NotBlank(message = "El nombre de la categoría es obligatorio")
  @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
  @Column(name = "nombre_categoria", nullable = false, length = 50)
  private String nombre;

  @Size(max = 10, message = "El icono no puede exceder los 10 caracteres")
  @Column(name = "icono", length = 10)
  private String icono;

  @Column(name = "descripcion", columnDefinition = "TEXT")
  private String descripcion;

  @Size(max = 255, message = "La URL de imagen no puede exceder los 255 caracteres")
  @Column(name = "imagen_url", length = 255)
  private String imagenUrl;

  @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Producto> productos;
}