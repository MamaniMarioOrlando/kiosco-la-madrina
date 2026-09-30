package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Corre contra un servidor HTTP real (no MockMvc).
 * MockMvc no ejecuta el forward interno a /error que hace el contenedor tras un sendError(),
 * así que no detecta bugs donde un 403 termina llegando al cliente como 401.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RealServerSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void employee_AccessingAdminEndpoint_Gets403_Not401() {
        String adminToken = login("admin", "AdminPass123!");
        String username = "emp_" + UUID.randomUUID().toString().substring(0, 8);
        ResponseEntity<String> created = rest.exchange("/api/users", HttpMethod.POST, json(adminToken, Map.of(
                "username", username, "email", username + "@kiosco.test",
                "password", "EmployeePass123", "role", "EMPLOYEE")), String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> response = rest.exchange("/api/users", HttpMethod.GET,
                json(login(username, "EmployeePass123"), null), String.class);

        // Un 401 haría que el frontend cierre la sesión de un empleado que solo no tiene permiso.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        // Mismo formato JSON que el resto de los errores de la API, con un mensaje apto para mostrar.
        assertThat(JsonPath.<String>read(response.getBody(), "$.message"))
                .isEqualTo("No tienes permisos suficientes para realizar esta acción.");
        assertThat(JsonPath.<String>read(response.getBody(), "$.path")).isEqualTo("/api/users");
    }

    @Test
    void requestWithoutToken_Gets401() {
        ResponseEntity<String> response = rest.getForEntity("/api/products", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(JsonPath.<String>read(response.getBody(), "$.message"))
                .isEqualTo("Tu sesión no es válida o expiró. Iniciá sesión nuevamente.");
    }

    private String login(String username, String password) {
        ResponseEntity<String> response = rest.postForEntity("/api/auth/signin",
                Map.of("username", username, "password", password), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return JsonPath.read(response.getBody(), "$.token");
    }

    private static HttpEntity<Object> json(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
