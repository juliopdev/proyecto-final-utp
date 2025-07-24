package com.utp.proyectofinal.models.documents;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "comentarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Comentario extends BaseDocument {

    @NotBlank(message = "El ID del cliente es obligatorio")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Debe ser un UUID válido")
    @Indexed
    @Field("id_cliente")
    private String idCliente;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 1, message = "El nombre del cliente no puede estar vacío")
    @Field("nombre_cliente") 
    private String nombreCliente;

    @NotBlank(message = "El comentario es obligatorio")
    @Size(min = 10, max = 500, message = "El comentario debe tener entre 10 y 500 caracteres")
    private String comentario;

    @Min(value = 1, message = "La puntuación debe ser al menos 1")
    @Max(value = 5, message = "La puntuación no puede exceder 5")
    private int puntuacion;
}