package com.utp.proyectofinal.repositories.postgresql;

import com.utp.proyectofinal.models.entities.User;
import com.utp.proyectofinal.models.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User en PostgreSQL
 * 
 * PATRONES IMPLEMENTADOS:
 * - REPOSITORY: Patrón Repository para abstracción de acceso a datos
 * - FACTORY: JPA genera implementaciones automáticamente
 * - SPECIFICATION: Query methods para consultas específicas
 * - PROXY: Spring Data JPA actúa como proxy para las implementaciones
 * 
 * @author Julio Pariona
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * PATRÓN SPECIFICATION: Buscar por email excluyendo eliminados
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * PATRÓN SPECIFICATION: Buscar por ID excluyendo eliminados
     */
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    /**
     * PATRÓN SPECIFICATION: Buscar por teléfono excluyendo eliminados
     */
    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

    /**
     * PATRÓN SPECIFICATION: Buscar por MongoDB ID
     */
    Optional<User> findByMongoUserIdAndDeletedAtIsNull(String mongoUserId);

    /**
     * PATRÓN SPECIFICATION: Verificar si existe email
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * PATRÓN SPECIFICATION: Verificar si existe teléfono
     */
    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios activos
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.enabled = true")
    List<User> findAllActiveUsers();

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios por rol
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    List<User> findByRoleAndDeletedAtIsNull(@Param("role") Role role);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios por rol con paginación
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    Page<User> findByRoleAndDeletedAtIsNull(@Param("role") Role role, Pageable pageable);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios por estado
     */
    @Query("SELECT u FROM User u WHERE u.enabled = :enabled AND u.deletedAt IS NULL")
    List<User> findByEnabledAndDeletedAtIsNull(@Param("enabled") Boolean enabled);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios verificados
     */
    @Query("SELECT u FROM User u WHERE u.verified = :verified AND u.deletedAt IS NULL")
    List<User> findByVerifiedAndDeletedAtIsNull(@Param("verified") Boolean verified);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios bloqueados
     */
    @Query("SELECT u FROM User u WHERE u.locked = :locked AND u.deletedAt IS NULL")
    List<User> findByLockedAndDeletedAtIsNull(@Param("locked") Boolean locked);

    /**
     * PATRÓN SPECIFICATION: Buscar por nombre completo (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND u.deletedAt IS NULL")
    List<User> findByFullNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios registrados en un rango de fechas
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate AND u.deletedAt IS NULL")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios con último login en rango
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin BETWEEN :startDate AND :endDate AND u.deletedAt IS NULL")
    List<User> findByLastLoginBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * PATRÓN SPECIFICATION: Contar usuarios activos por rol
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.enabled = true AND u.deletedAt IS NULL")
    long countActiveUsersByRole(@Param("role") Role role);

    /**
     * PATRÓN SPECIFICATION: Contar usuarios registrados hoy
     */
    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.createdAt) = CURRENT_DATE AND u.deletedAt IS NULL")
    long countUsersRegisteredToday();

    /**
     * PATRÓN COMMAND: Actualizar último login
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * PATRÓN COMMAND: Actualizar estado de verificación
     */
    @Modifying
    @Query("UPDATE User u SET u.verified = :verified WHERE u.id = :userId")
    void updateVerificationStatus(@Param("userId") Long userId, @Param("verified") Boolean verified);

    /**
     * PATRÓN COMMAND: Actualizar estado habilitado
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    void updateEnabledStatus(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * PATRÓN COMMAND: Bloquear/desbloquear usuario
     */
    @Modifying
    @Query("UPDATE User u SET u.locked = :locked WHERE u.id = :userId")
    void updateLockedStatus(@Param("userId") Long userId, @Param("locked") Boolean locked);

    /**
     * PATRÓN COMMAND: Soft delete por ID
     */
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = CURRENT_TIMESTAMP, u.enabled = false WHERE u.id = :userId")
    void softDeleteById(@Param("userId") Long userId);

    /**
     * PATRÓN COMMAND: Actualizar MongoDB User ID
     */
    @Modifying
    @Query("UPDATE User u SET u.mongoUserId = :mongoUserId WHERE u.id = :userId")
    void updateMongoUserId(@Param("userId") Long userId, @Param("mongoUserId") String mongoUserId);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios para sincronización (sin MongoDB ID)
     */
    @Query("SELECT u FROM User u WHERE u.mongoUserId IS NULL AND u.deletedAt IS NULL")
    List<User> findUsersForMongoSync();

    /**
     * PATRÓN SPECIFICATION: Buscar todos excluyendo eliminados con paginación
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findAllActiveUsers(Pageable pageable);

    /**
     * PATRÓN SPECIFICATION: Búsqueda global de usuarios
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "u.phone LIKE CONCAT('%', :searchTerm, '%')) " +
           "AND u.deletedAt IS NULL")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}