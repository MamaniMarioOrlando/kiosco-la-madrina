package devMario.example.kioscoLaMadrina.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

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
}
