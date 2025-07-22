package com.utp.proyectofinal.repositories.mongodb;

import com.utp.proyectofinal.models.documents.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para UserDocument en MongoDB
 * 
 * PATRONES IMPLEMENTADOS:
 * - REPOSITORY: Patrón Repository para abstracción de acceso a datos NoSQL
 * - FACTORY: Spring Data MongoDB genera implementaciones automáticamente
 * - SPECIFICATION: Query methods para consultas específicas en MongoDB
 * - PROXY: Spring Data MongoDB actúa como proxy
 * 
 * @author Julio Pariona
 */
@Repository
public interface UserDocumentRepository extends MongoRepository<UserDocument, String> {

    /**
     * PATRÓN SPECIFICATION: Buscar por email
     */
    Optional<UserDocument> findByEmail(String email);

    /**
     * PATRÓN SPECIFICATION: Buscar por username
     */
    Optional<UserDocument> findByUsername(String username);

    /**
     * PATRÓN SPECIFICATION: Buscar por ID de PostgreSQL
     */
    Optional<UserDocument> findByPostgresUserId(Long postgresUserId);

    /**
     * PATRÓN SPECIFICATION: Verificar si existe por email
     */
    boolean existsByEmail(String email);

    /**
     * PATRÓN SPECIFICATION: Verificar si existe por username
     */
    boolean existsByUsername(String username);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios por estado
     */
    List<UserDocument> findByStatus(String status);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios verificados
     */
    List<UserDocument> findByIsVerified(Boolean isVerified);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios por rol
     */
    List<UserDocument> findByRolesContaining(String role);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios activos
     */
    @Query("{ 'status': 'ACTIVE' }")
    List<UserDocument> findActiveUsers();

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios activos con paginación
     */
    @Query("{ 'status': 'ACTIVE' }")
    Page<UserDocument> findActiveUsers(Pageable pageable);

    /**
     * PATRÓN SPECIFICATION: Buscar por ciudad en dirección
     */
    @Query("{ 'address.city': ?0 }")
    List<UserDocument> findByAddressCity(String city);

    /**
     * PATRÓN SPECIFICATION: Buscar por estado/departamento
     */
    @Query("{ 'address.state': ?0 }")
    List<UserDocument> findByAddressState(String state);

    /**
     * PATRÓN SPECIFICATION: Buscar por país
     */
    @Query("{ 'address.country': ?0 }")
    List<UserDocument> findByAddressCountry(String country);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios registrados en rango de fechas
     */
    @Query("{ 'registrationDate': { $gte: ?0, $lte: ?1 } }")
    List<UserDocument> findByRegistrationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios con último login en rango
     */
    @Query("{ 'lastLogin': { $gte: ?0, $lte: ?1 } }")
    List<UserDocument> findByLastLoginBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios con notificaciones email habilitadas
     */
    @Query("{ 'notificationPreferences.emailAlerts': true }")
    List<UserDocument> findUsersWithEmailNotifications();

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios con notificaciones in-app habilitadas
     */
    @Query("{ 'notificationPreferences.inAppNotifications': true }")
    List<UserDocument> findUsersWithInAppNotifications();

    /**
     * PATRÓN SPECIFICATION: Buscar por nombre completo (case insensitive)
     */
    @Query("{ $or: [ " +
           "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'lastName': { $regex: ?0, $options: 'i' } }, " +
           "{ $expr: { $regexMatch: { input: { $concat: ['$firstName', ' ', '$lastName'] }, regex: ?0, options: 'i' } } } " +
           "] }")
    List<UserDocument> findByFullNameContainingIgnoreCase(String searchTerm);

    /**
     * PATRÓN SPECIFICATION: Búsqueda de texto completo
     */
    @Query("{ $or: [ " +
           "{ 'username': { $regex: ?0, $options: 'i' } }, " +
           "{ 'email': { $regex: ?0, $options: 'i' } }, " +
           "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'lastName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'phoneNumber': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<UserDocument> searchUsers(String searchTerm);

    /**
     * PATRÓN SPECIFICATION: Búsqueda de texto completo con paginación
     */
    @Query("{ $or: [ " +
           "{ 'username': { $regex: ?0, $options: 'i' } }, " +
           "{ 'email': { $regex: ?0, $options: 'i' } }, " +
           "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'lastName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'phoneNumber': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<UserDocument> searchUsers(String searchTerm, Pageable pageable);

    /**
     * PATRÓN SPECIFICATION: Contar usuarios por estado
     */
    long countByStatus(String status);

    /**
     * PATRÓN SPECIFICATION: Contar usuarios verificados
     */
    long countByIsVerified(Boolean isVerified);

    /**
     * PATRÓN SPECIFICATION: Contar usuarios por rol
     */
    long countByRolesContaining(String role);

    /**
     * PATRÓN SPECIFICATION: Contar usuarios registrados hoy
     */
    @Query(value = "{ 'registrationDate': { $gte: ?0, $lt: ?1 } }", count = true)
    long countUsersRegisteredToday(LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios sin referencia a PostgreSQL
     */
    @Query("{ 'postgresUserId': { $exists: false } }")
    List<UserDocument> findUsersWithoutPostgresReference();

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios para sincronización
     */
    @Query("{ $or: [ " +
           "{ 'postgresUserId': { $exists: false } }, " +
           "{ 'postgresUserId': null } " +
           "] }")
    List<UserDocument> findUsersForPostgresSync();

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios inactivos por tiempo
     */
    @Query("{ 'lastLogin': { $lt: ?0 }, 'status': 'ACTIVE' }")
    List<UserDocument> findInactiveUsers(LocalDateTime cutoffDate);

    /**
     * PATRÓN SPECIFICATION: Buscar usuarios por metadata específica
     */
    @Query("{ 'metadata.?0': ?1 }")
    List<UserDocument> findByMetadataField(String fieldName, Object value);

    /**
     * PATRÓN SPECIFICATION: Agregación para estadísticas de usuarios por ciudad
     */
    @Query(value = "{ 'address.city': { $exists: true, $ne: null } }", 
           fields = "{ 'address.city': 1, '_id': 0 }")
    List<UserDocument> findCitiesWithUsers();
}