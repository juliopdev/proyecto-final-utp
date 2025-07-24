package com.utp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProyectoFinalUtpApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProyectoFinalUtpApplication.class, args);
    System.out.println("🚀 Proyecto Final UTP - Aplicación iniciada correctamente");
    System.out.println("📱 Accede a: http://localhost:8080");
    System.out.println("🔧 Perfil activo: " + System.getProperty("spring.profiles.active", "default"));
  }
}