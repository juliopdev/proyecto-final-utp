package com.utp.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuración de MongoDB
 * 
 * Configura:
 * - Cliente MongoDB
 * - MongoTemplate
 * - Configuración de base de datos
 * 
 * @author Julio Pariona
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.utp.proyectofinal.repositories.mongodb")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    /**
     * Configuración adicional para el cliente MongoDB
     * Incluye configuraciones de conexión, timeout, etc.
     */
    @Override
    protected void configureClientSettings(com.mongodb.MongoClientSettings.Builder builder) {
        builder.applyToConnectionPoolSettings(connectionPoolBuilder -> 
            connectionPoolBuilder
                .maxSize(20)
                .minSize(5)
                .maxWaitTime(2000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .maxConnectionIdleTime(30000, java.util.concurrent.TimeUnit.MILLISECONDS)
        );
        
        builder.applyToSocketSettings(socketBuilder ->
            socketBuilder
                .connectTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(10000, java.util.concurrent.TimeUnit.MILLISECONDS)
        );
    }
}