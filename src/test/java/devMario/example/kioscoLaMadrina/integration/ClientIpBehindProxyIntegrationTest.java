package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * En producción las conexiones llegan a través del proxy de Railway, que informa la IP del cliente en
 * X-Forwarded-For. Corre contra el servidor real porque esa lógica vive en Tomcat (MockMvc no la ejecuta).
 * El test se conecta desde 127.0.0.1, una dirección interna que Tomcat trata como proxy de confianza.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ClientIpBehindProxyIntegrationTest extends AbstractIntegrationTest {

    private static final String EMPLOYEE_PASSWORD = "EmployeePass123";

    @Autowired
    private TestRestTemplate rest;

    @Test
    void clientsBehindTheProxyAreLimitedSeparately() {
        String username = createEmployee();
        failFiveTimes(username, "203.0.113.10");

        // Otro cliente detrás del mismo proxy no hereda el bloqueo.
        assertThat(login(username, "incorrecta", "203.0.113.20").getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void aSpoofedForwardedForCannotDodgeTheLimit() {
        String username = createEmployee();
        // El atacante inventa una IP distinta en cada intento; el proxy agrega la real al final.
        for (int i = 0; i < 5; i++) {
            login(username, "incorrecta", "1.1.1." + i + ", 198.51.100.7");
        }

        assertThat(login(username, "incorrecta", "9.9.9.9, 198.51.100.7").getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    private void failFiveTimes(String username, String forwardedFor) {
        for (int i = 0; i < 5; i++) {
            assertThat(login(username, "incorrecta", forwardedFor).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
        assertThat(login(username, "incorrecta", forwardedFor).getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    private String createEmployee() {
        String adminToken = JsonPath.read(login("admin", "AdminPass123!", "203.0.113.99").getBody(), "$.token");
        String username = "emp_" + UUID.randomUUID().toString().substring(0, 8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        Map<String, String> body = Map.of("username", username, "email", username + "@kiosco.test",
                "password", EMPLOYEE_PASSWORD, "role", "EMPLOYEE");
        assertThat(rest.exchange("/api/users", HttpMethod.POST, new HttpEntity<>(body, headers), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return username;
    }

    private ResponseEntity<String> login(String username, String password, String forwardedFor) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Forwarded-For", forwardedFor);
        return rest.postForEntity("/api/auth/signin",
                new HttpEntity<>(Map.of("username", username, "password", password), headers), String.class);
    }
}
