package com.utp.proyectofinal.models.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseDocument {

    @Id
    @EqualsAndHashCode.Include
    private ObjectId id;

    @CreatedDate
    @Field(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Field(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}