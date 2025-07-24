package com.utp.proyectofinal.repositories.postgresql;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp.proyectofinal.models.entities.Producto;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
}