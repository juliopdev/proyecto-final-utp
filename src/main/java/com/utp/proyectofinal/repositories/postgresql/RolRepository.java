package com.utp.proyectofinal.repositories.postgresql;

import com.utp.proyectofinal.models.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {

    /**
     * Busca un rol por su nombre
     * @param nombreRol nombre del rol
     * @return Optional con el rol si existe
     */
    Optional<Rol> findByNombreRol(String nombreRol);

    /**
     * Verifica si existe un rol con el nombre dado
     * @param nombreRol nombre del rol a verificar
     * @return true si existe, false si no
     */
    boolean existsByNombreRol(String nombreRol);
}