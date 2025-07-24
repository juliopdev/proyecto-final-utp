# --- ETAPA 1: Construcción (Build Stage) ---
# Usamos una imagen oficial de Maven que ya incluye Java 17 para construir el proyecto.
# Le ponemos un alias "build" a esta etapa.
FROM maven:3.9.6-eclipse-temurin-17-focal AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos solo el pom.xml para aprovechar el cache de Docker.
# Si las dependencias no cambian, Docker no las volverá a descargar.
COPY pom.xml .

# Descargamos todas las dependencias
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente del proyecto
COPY src ./src

# Construimos la aplicación, omitiendo las pruebas para un despliegue más rápido
RUN mvn clean install -DskipTests


# --- ETAPA 2: Ejecución (Runtime Stage) ---
# Usamos una imagen de Java mucho más ligera, solo con lo necesario para ejecutar.
FROM eclipse-temurin:17-jre-focal

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos únicamente el archivo .jar construido desde la etapa anterior ("build")
COPY --from=build /app/target/proyecto-final-utp-0.0.1-SNAPSHOT.jar .

# Exponemos el puerto en el que corre Spring Boot dentro del contenedor
EXPOSE 8080

# Este es el comando que se ejecutará para iniciar tu aplicación cuando el contenedor arranque
ENTRYPOINT ["java", "-jar", "proyecto-final-utp-0.0.1-SNAPSHOT.jar"]