package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.security.TrustedProxyFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Detrás del proxy de Next.js, todos los pedidos llegan desde la IP de Vercel. Sin este mecanismo,
 * 5 contraseñas mal de una persona bloquearían el login de todo el kiosco.
 */
@ActiveProfiles("test")
class TrustedProxyLoginIntegrationTest extends AbstractIntegrationTest {

    private static final String VERCEL_IP = "76.76.21.21";
    private static final String EMPLOYEE_PASSWORD = "EmployeePass123";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clientsBehindTheProxyAreLimitedSeparately() throws Exception {
        String username = createEmployee();
        for (int i = 0; i < 5; i++) {
            loginThroughProxy(PROXY_SECRET, "203.0.113.30", username, "incorrecta");
        }
        loginThroughProxy(PROXY_SECRET, "203.0.113.30", username, "incorrecta")
                .andExpect(status().isTooManyRequests());

        // Otro cliente que entra por el mismo proxy (misma IP de Vercel) no hereda el bloqueo.
        loginThroughProxy(PROXY_SECRET, "203.0.113.31", username, EMPLOYEE_PASSWORD)
                .andExpect(status().isOk());
    }

    @Test
    void withoutTheSecret_AnInventedClientIpCannotDodgeTheLimit() throws Exception {
        String username = createEmployee();
        // El atacante le pega directo a Railway e inventa una IP distinta en cada intento.
        for (int i = 0; i < 5; i++) {
            loginThroughProxy("adivinando", "1.1.1." + i, username, "incorrecta");
        }

        loginThroughProxy("adivinando", "9.9.9.9", username, "incorrecta")
                .andExpect(status().isTooManyRequests());
    }

    private String createEmployee() throws Exception {
        String adminToken = JsonPath.read(loginThroughProxy(PROXY_SECRET, "203.0.113.99", "admin", "AdminPass123!")
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

    private ResultActions loginThroughProxy(String secret, String clientIp, String username, String password)
            throws Exception {
        return mockMvc.perform(post("/api/auth/signin")
                .with(request -> {
                    request.setRemoteAddr(VERCEL_IP);
                    return request;
                })
                .header(TrustedProxyFilter.SECRET_HEADER, secret)
                .header(TrustedProxyFilter.CLIENT_IP_HEADER, clientIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)));
    }
}
