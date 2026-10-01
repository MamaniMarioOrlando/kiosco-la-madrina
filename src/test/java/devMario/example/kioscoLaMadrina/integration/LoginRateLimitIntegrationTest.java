package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fuerza bruta contra el login: tras 5 contraseñas incorrectas, la cuenta queda bloqueada para esa IP.
 * Cada test usa su propia IP para no interferir con los demás (todos comparten el mismo contexto).
 */
@ActiveProfiles("test")
class LoginRateLimitIntegrationTest extends AbstractIntegrationTest {

    private static final String EMPLOYEE_PASSWORD = "EmployeePass123";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void afterFiveWrongPasswords_EvenTheCorrectPasswordIsRejectedWith429() throws Exception {
        String ip = "10.20.0.1";
        String username = createEmployee();
        for (int i = 0; i < 5; i++) {
            login(ip, username, "incorrecta").andExpect(status().isUnauthorized());
        }

        // Se bloquea ANTES de verificar la contraseña: si no, el atacante sabría cuándo acertó.
        MvcResult blocked = login(ip, username, EMPLOYEE_PASSWORD)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(containsString("Demasiados intentos")))
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andReturn();

        int retryAfterSeconds = Integer.parseInt(blocked.getResponse().getHeader(HttpHeaders.RETRY_AFTER));
        assertThat(retryAfterSeconds).isBetween(1, 15 * 60);
    }

    @Test
    void theSameAccountCanStillLogInFromAnotherNetwork() throws Exception {
        String username = createEmployee();
        for (int i = 0; i < 5; i++) {
            login("10.20.0.2", username, "incorrecta");
        }

        login("10.20.0.3", username, EMPLOYEE_PASSWORD).andExpect(status().isOk());
    }

    @Test
    void successfulLoginResetsTheCounter() throws Exception {
        String ip = "10.20.0.4";
        String username = createEmployee();
        for (int i = 0; i < 4; i++) {
            login(ip, username, "incorrecta");
        }
        login(ip, username, EMPLOYEE_PASSWORD).andExpect(status().isOk());

        for (int i = 0; i < 4; i++) {
            login(ip, username, "incorrecta").andExpect(status().isUnauthorized());
        }
        login(ip, username, EMPLOYEE_PASSWORD).andExpect(status().isOk());
    }

    private String createEmployee() throws Exception {
        String adminToken = JsonPath.read(login("10.20.9.9", "admin", "AdminPass123!")
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.token");
        String username = "emp_" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@kiosco.test","password":"%s","role":"EMPLOYEE"}
                                """.formatted(username, username, EMPLOYEE_PASSWORD)))
                .andExpect(status().isCreated());
        return username;
    }

    private ResultActions login(String ip, String username, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/signin")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)));
    }
}
