package com.utp.proyectofinal.repositories.mongodb;

import com.utp.proyectofinal.models.documents.LogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para LogDocument en MongoDB
 * 
 * PATRONES IMPLEMENTADOS:
 * - REPOSITORY: Patrón Repository para logs y auditoría
 * - OBSERVER: Almacena eventos observados del sistema
 * - MEMENTO: Registra estados históricos para auditoría
 * - FACTORY: Spring Data MongoDB genera implementaciones
 * 
 * @author Julio Pariona
 */
@Repository
public interface LogDocumentRepository extends MongoRepository<LogDocument, String> {

  /**
   * PATRÓN SPECIFICATION: Buscar logs por tipo de evento
   */
  List<LogDocument> findByEventType(String eventType);

  /**
   * PATRÓN SPECIFICATION: Buscar logs por usuario
   */
  List<LogDocument> findByUserId(String userId);

  /**
   * PATRÓN SPECIFICATION: Buscar logs por email de usuario
   */
  List<LogDocument> findByUserEmail(String userEmail);

  /**
   * PATRÓN SPECIFICATION: Buscar logs por nivel
   */
  List<LogDocument> findByLevel(String level);

  /**
   * PATRÓN SPECIFICATION: Buscar logs por módulo
   */
  List<LogDocument> findByModule(String module);

  /**
   * PATRÓN SPECIFICATION: Buscar logs en rango de fechas
   */
  List<LogDocument> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN SPECIFICATION: Buscar logs en rango de fechas con paginación
   */
  Page<LogDocument> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  /**
   * PATRÓN SPECIFICATION: Buscar logs por IP
   */
  List<LogDocument> findByIpAddress(String ipAddress);

  /**
   * PATRÓN SPECIFICATION: Buscar logs de error
   */
  @Query("{ 'level': 'ERROR' }")
  List<LogDocument> findErrorLogs();

  /**
   * PATRÓN SPECIFICATION: Buscar logs de error con paginación
   */
  @Query("{ 'level': 'ERROR' }")
  Page<LogDocument> findErrorLogs(Pageable pageable);

  /**
   * PATRÓN SPECIFICATION: Buscar logs de autenticación
   */
  @Query("{ 'module': 'AUTH' }")
  List<LogDocument> findAuthenticationLogs();

  /**
   * PATRÓN SPECIFICATION: Buscar logs de seguridad
   */
  @Query("{ $or: [ " +
      "{ 'eventType': { $regex: 'LOGIN', $options: 'i' } }, " +
      "{ 'eventType': { $regex: 'UNAUTHORIZED', $options: 'i' } }, " +
      "{ 'eventType': { $regex: 'SUSPICIOUS', $options: 'i' } } " +
      "] }")
  List<LogDocument> findSecurityLogs();

  /**
   * PATRÓN SPECIFICATION: Buscar logs de seguridad con paginación
   */
  @Query("{ $or: [ " +
      "{ 'eventType': { $regex: 'LOGIN', $options: 'i' } }, " +
      "{ 'eventType': { $regex: 'UNAUTHORIZED', $options: 'i' } }, " +
      "{ 'eventType': { $regex: 'SUSPICIOUS', $options: 'i' } } " +
      "] }")
  Page<LogDocument> findSecurityLogs(Pageable pageable);

  /**
   * PATRÓN SPECIFICATION: Buscar logs de usuario específico en rango
   */
  @Query("{ 'userEmail': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
  List<LogDocument> findUserLogsInDateRange(String userEmail, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN SPECIFICATION: Buscar logs por tipo de evento y usuario
   */
  @Query("{ 'eventType': ?0, 'userEmail': ?1 }")
  List<LogDocument> findByEventTypeAndUserEmail(String eventType, String userEmail);

  /**
   * PATRÓN SPECIFICATION: Buscar intentos de login fallidos
   */
  @Query("{ 'eventType': 'LOGIN_FAILED' }")
  List<LogDocument> findFailedLoginAttempts();

  /**
   * PATRÓN SPECIFICATION: Buscar intentos de login fallidos por IP
   */
  @Query("{ 'eventType': 'LOGIN_FAILED', 'ipAddress': ?0 }")
  List<LogDocument> findFailedLoginAttemptsByIp(String ipAddress);

  /**
   * PATRÓN SPECIFICATION: Buscar intentos de login fallidos en rango de tiempo
   */
  @Query("{ 'eventType': 'LOGIN_FAILED', 'timestamp': { $gte: ?0, $lte: ?1 } }")
  List<LogDocument> findFailedLoginAttemptsInTimeRange(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN SPECIFICATION: Buscar logs de actividad sospechosa
   */
  @Query("{ 'eventType': { $in: ['SUSPICIOUS_ACTIVITY', 'UNAUTHORIZED_ACCESS'] } }")
  List<LogDocument> findSuspiciousActivity();

  /**
   * PATRÓN SPECIFICATION: Buscar logs de acceso a API
   */
  @Query("{ 'eventType': 'API_ACCESS' }")
  List<LogDocument> findApiAccessLogs();

  /**
   * PATRÓN SPECIFICATION: Buscar logs por session ID
   */
  List<LogDocument> findBySessionId(String sessionId);

  /**
   * PATRÓN SPECIFICATION: Buscar logs recientes (últimas 24 horas)
   */
  @Query("{ 'timestamp': { $gte: ?0 } }")
  List<LogDocument> findRecentLogs(LocalDateTime since24HoursAgo);

  /**
   * PATRÓN SPECIFICATION: Buscar logs por descripción (búsqueda de texto)
   */
  @Query("{ 'description': { $regex: ?0, $options: 'i' } }")
  List<LogDocument> findByDescriptionContaining(String searchTerm);

  /**
   * PATRÓN SPECIFICATION: Contar logs por tipo de evento
   */
  long countByEventType(String eventType);

  /**
   * PATRÓN SPECIFICATION: Contar logs por nivel
   */
  long countByLevel(String level);

  /**
   * PATRÓN SPECIFICATION: Contar logs por módulo
   */
  long countByModule(String module);

  /**
   * PATRÓN SPECIFICATION: Contar logs de error en las últimas 24 horas
   */
  @Query(value = "{ 'level': 'ERROR', 'timestamp': { $gte: ?0 } }", count = true)
  long countErrorLogsLast24Hours(LocalDateTime since24HoursAgo);

  /**
   * PATRÓN SPECIFICATION: Contar intentos de login fallidos por usuario en tiempo
   * específico
   */
  @Query(value = "{ 'eventType': 'LOGIN_FAILED', 'userEmail': ?0, 'timestamp': { $gte: ?1 } }", count = true)
  long countFailedLoginAttempts(String userEmail, LocalDateTime since);

  /**
   * PATRÓN SPECIFICATION: Contar intentos de login fallidos por IP en tiempo
   * específico
   */
  @Query(value = "{ 'eventType': 'LOGIN_FAILED', 'ipAddress': ?0, 'timestamp': { $gte: ?1 } }", count = true)
  long countFailedLoginAttemptsByIp(String ipAddress, LocalDateTime since);

  /**
   * PATRÓN SPECIFICATION: Contar logs en rango de fechas
   */
  long countByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * PATRÓN SPECIFICATION: Buscar logs por tipo de evento y rango de fechas
   */
  List<LogDocument> findByEventTypeAndTimestampBetween(String eventType, LocalDateTime startDate,
      LocalDateTime endDate);

  /**
   * PATRÓN SPECIFICATION: Buscar logs ordenados por timestamp descendente
   */
  List<LogDocument> findByOrderByTimestampDesc();

  /**
   * PATRÓN SPECIFICATION: Buscar los últimos N logs
   */
  List<LogDocument> findTop100ByOrderByTimestampDesc();

  /**
   * PATRÓN SPECIFICATION: Buscar logs por referencia a PostgreSQL
   */
  List<LogDocument> findByPostgresReferenceId(Long postgresReferenceId);

  /**
   * PATRÓN SPECIFICATION: Buscar logs con detalles específicos
   */
  @Query("{ 'details.?0': ?1 }")
  List<LogDocument> findByDetailField(String fieldName, Object value);

  /**
   * PATRÓN SPECIFICATION: Buscar logs sin detalles (básicos)
   */
  @Query("{ 'details': { $exists: false } }")
  List<LogDocument> findLogsWithoutDetails();

  /**
   * PATRÓN SPECIFICATION: Estadísticas de logs por hora del día
   */
  @Query(value = "{ 'timestamp': { $gte: ?0, $lte: ?1 } }", fields = "{ 'timestamp': 1, 'eventType': 1, '_id': 0 }")
  List<LogDocument> findLogsForStats(LocalDateTime startDate, LocalDateTime endDate);
}