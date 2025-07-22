package com.utp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Aplicación principal del Proyecto Final UTP
 * 
 * Esta clase configura y ejecuta la aplicación Spring Boot con soporte para:
 * - PostgreSQL (JPA/Hibernate)
 * - MongoDB
 * - Spring Security
 * - Thymeleaf SSR
 * 
 * @author Julio Pariona
 * @version 1.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.utp.proyectofinal.repositories.postgresql")
@EnableMongoRepositories(basePackages = "com.utp.proyectofinal.repositories.mongodb")
public class ProyectoFinalUtpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProyectoFinalUtpApplication.class, args);
        System.out.println("🚀 Proyecto Final UTP - Aplicación iniciada correctamente");
        System.out.println("📱 Accede a: http://localhost:8080");
        System.out.println("🔧 Perfil activo: " + System.getProperty("spring.profiles.active", "default"));
    }
}