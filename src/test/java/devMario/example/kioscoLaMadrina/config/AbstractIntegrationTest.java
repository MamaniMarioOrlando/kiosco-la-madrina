package devMario.example.kioscoLaMadrina.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * 🏰 LA FORTALEZA BASE (S.O.L.I.D. - Principio Open/Closed)
 * 
 * Todo Test de Integración debe heredar ("extends") de esta clase.
 * Esto garantiza que el motor Docker de PostgreSQL se arranque UNA SOLA VEZ
 * y se reutilice, evitando perder minutos valiosos levantando contenedores 
 * por cada test individual.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

    // 🐋 El Motor de Base de Datos Real pero descartable
    // La anotación @ServiceConnection es pura magia de Spring Boot 3.1+:
    // Inyectará el jdbc-url, username y password dinámico del Docker directamente a Spring.
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

}
