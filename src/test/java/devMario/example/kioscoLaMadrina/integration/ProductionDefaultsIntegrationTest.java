package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Seguro por defecto: sin ningún perfil activo (así corre el .jar en Railway) no se publica el mapa
 * de la API ni se vuelca el SQL a los logs. Las herramientas de desarrollo requieren el perfil "dev".
 */
@ActiveProfiles("test")
class ProductionDefaultsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Test
    void apiDocumentationIsNotPublished() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    }

    @Test
    void sqlIsNotLogged() {
        assertThat(environment.getProperty("spring.jpa.show-sql", Boolean.class, false)).isFalse();
    }

    @Test
    void internalErrorMessagesAreNotExposed() {
        assertThat(environment.getProperty("server.error.include-message", "never")).isEqualTo("never");
    }
}
