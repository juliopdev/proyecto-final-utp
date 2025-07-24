package com.utp.proyectofinal.repositories.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;
import com.utp.proyectofinal.models.entities.Categoria;

import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
}