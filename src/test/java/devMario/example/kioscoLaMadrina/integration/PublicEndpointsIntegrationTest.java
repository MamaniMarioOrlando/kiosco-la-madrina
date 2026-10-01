package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Mínimo privilegio: solo el login es público. Cualquier otra ruta de la API exige token,
 * incluso las que todavía no existen, para que un endpoint nuevo nunca quede abierto por accidente.
 */
@ActiveProfiles("test")
class PublicEndpointsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_IsPublic() throws Exception {
        // 400 (body inválido) y no 401: la petición llegó al controller sin pedir token.
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void otherRoutesUnderAuth_RequireToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithAnotherHttpMethod_RequiresToken() throws Exception {
        mockMvc.perform(get("/api/auth/signin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void legacyTestRoutes_RequireToken() throws Exception {
        mockMvc.perform(get("/api/test/ping"))
                .andExpect(status().isUnauthorized());
    }
}
