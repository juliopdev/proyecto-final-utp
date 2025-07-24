package com.utp.proyectofinal.models.documents;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "clientes_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClienteInfo extends BaseDocument {

    @NotBlank(message = "El ID del cliente es obligatorio")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Debe ser un UUID válido")
    @Indexed(unique = true)
    @Field("id_cliente")
    private String idCliente;

    @Valid
    @Field("comentarios_generales")
    private List<ComentarioGeneral> comentariosGenerales;

    @Valid
    private Preferencias preferencias;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComentarioGeneral {
        @NotBlank(message = "El comentario no puede estar vacío")
        @Size(min = 1, message = "El comentario no puede estar vacío")
        private String texto;

        private LocalDateTime fecha;

        @jakarta.validation.constraints.Min(value = 1, message = "La puntuación debe ser al menos 1")
        @jakarta.validation.constraints.Max(value = 5, message = "La puntuación no puede exceder 5")
        private int puntuacion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Preferencias {
        @NotBlank(message = "El idioma es obligatorio")
        @Pattern(regexp = "^(ES|EN)$", message = "El idioma debe ser ES o EN")
        private String idioma;

        @NotBlank(message = "El método de pago preferido es obligatorio")
        @Pattern(regexp = "^(tarjeta|yape|efectivo)$", message = "El método de pago debe ser tarjeta, yape o efectivo")
        @Field("metodo_pago_preferido")
        private String metodoPagoPreferido;

        @NotBlank(message = "El tipo de entrega preferido es obligatorio")
        @Pattern(regexp = "^(delivery|recojo|mesa)$", message = "El tipo de entrega debe ser delivery, recojo o mesa")
        @Field("tipo_entrega_preferido")
        private String tipoEntregaPreferido;

        private boolean notificaciones;
    }
}