package com.utp.proyectofinal.repositories.mongodb;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.utp.proyectofinal.models.documents.Comentario;

public interface ComentarioRepository extends MongoRepository<Comentario, ObjectId> {
}
