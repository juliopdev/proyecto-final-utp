package com.utp.proyectofinal.repositories.postgresql;

import com.utp.proyectofinal.models.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Role en PostgreSQL
 * 
 * PATRONES IMPLEMENTADOS:
 * - REPOSITORY: Patrón Repository para abstracción de acceso a datos
 * - FACTORY: Spring Data JPA genera implementaciones automáticamente
 * - SPECIFICATION: Query methods para consultas específicas
 * - PROXY: Spring Data JPA actúa como proxy para las implementaciones
 * 
 * @author Julio Pariona
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * PATRÓN SPECIFICATION: Buscar rol por nombre
     */
    Optional<Role> findByName(Role.RoleName name);

    /**
     * PATRÓN SPECIFICATION: Verificar si existe rol por nombre
     */
    boolean existsByName(Role.RoleName name);

    /**
     * PATRÓN SPECIFICATION: Obtener todos los roles ordenados por nombre
     */
    List<Role> findAllByOrderByName();

    /**
     * PATRÓN SPECIFICATION: Buscar roles por nombre que contenga texto
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Role> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * PATRÓN SPECIFICATION: Contar usuarios por rol
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId AND u.deletedAt IS NULL")
    long countUsersByRole(@Param("roleId") Long roleId);

    /**
     * PATRÓN SPECIFICATION: Obtener roles administrativos
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('ADMIN', 'VENDEDOR')")
    List<Role> findAdministrativeRoles();

    /**
     * PATRÓN SPECIFICATION: Obtener roles de usuario regular
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('USER', 'REPARTIDOR')")
    List<Role> findUserRoles();
}