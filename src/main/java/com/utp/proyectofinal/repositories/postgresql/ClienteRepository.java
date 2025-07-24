package com.utp.proyectofinal.repositories.postgresql;

import com.utp.proyectofinal.models.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    /**
     * Busca un cliente por email
     * @param email email del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Verifica si existe un cliente con el email dado
     * @param email email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Busca un cliente por email ignorando mayúsculas/minúsculas
     * @param email email del cliente
     * @return Optional con el cliente si existe
     */
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.email) = LOWER(:email)")
    Optional<Cliente> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Busca un cliente por DNI
     * @param dni DNI del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByDni(String dni);

    /**
     * Busca un cliente por RUC
     * @param ruc RUC del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByRuc(String ruc);

    /**
     * Verifica si existe un cliente con el DNI dado
     * @param dni DNI a verificar
     * @return true si existe, false si no
     */
    boolean existsByDni(String dni);

    /**
     * Verifica si existe un cliente con el RUC dado
     * @param ruc RUC a verificar
     * @return true si existe, false si no
     */
    boolean existsByRuc(String ruc);

    /**
     * Busca clientes por rol
     * @param nombreRol nombre del rol
     * @return Lista de clientes con ese rol
     */
    @Query("SELECT c FROM Cliente c JOIN c.rol r WHERE r.nombreRol = :nombreRol")
    Optional<Cliente> findByRolNombreRol(@Param("nombreRol") String nombreRol);
}