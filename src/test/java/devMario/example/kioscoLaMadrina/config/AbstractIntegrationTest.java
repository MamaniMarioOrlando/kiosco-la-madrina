package devMario.example.kioscoLaMadrina.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Base de todos los tests de integración.
 *
 * Usa el patrón "singleton container": un único PostgreSQL para toda la suite, arrancado una vez
 * y detenido por Testcontainers (Ryuk) al terminar la JVM.
 *
 * No usar @Testcontainers/@Container acá: detienen el contenedor al final de cada clase, pero Spring
 * reutiliza el contexto cacheado, que seguiría apuntando al puerto del contenedor ya detenido.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    /**
     * Clave JWT aleatoria por ejecución: ningún secreto queda commiteado en el repositorio,
     * ni siquiera uno de test (el repo es público y una clave commiteada podría terminar en producción).
     */
    private static final String JWT_SECRET = randomBase64Secret();

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("kiosco.app.jwtSecret", () -> JWT_SECRET);
    }

    private static String randomBase64Secret() {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }
}
