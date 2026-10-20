package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Errores del protocolo HTTP (método, tipo de contenido, parámetros) son errores del cliente:
 * deben responder su código 4xx con el formato JSON de la API, nunca 500.
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class HttpProtocolErrorsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void wrongHttpMethod_Returns405_WithAllowedMethods() throws Exception {
        mockMvc.perform(get("/api/auth/signin"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"))
                .andExpect(jsonPath("$.message").value("Método HTTP no permitido para esta ruta"))
                .andExpect(jsonPath("$.path").value("/api/auth/signin"));
    }

    @Test
    void unsupportedContentType_Returns415() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Bebidas"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Tipo de contenido no soportado: enviá application/json"));
    }

    @Test
    void pathVariableWithWrongType_Returns400() throws Exception {
        mockMvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valor inválido para 'id': abc"));
    }

    @Test
    void queryParameterWithWrongType_Returns400() throws Exception {
        mockMvc.perform(get("/api/sales").param("page", "uno"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valor inválido para 'page': uno"));
    }
}
