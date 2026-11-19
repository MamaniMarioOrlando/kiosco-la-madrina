package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sin CORS: la API no le da permiso a NINGÚN otro origen. El navegador le habla solo al frontend
 * (mismo origen, /api) y el proxy de Next reenvía de servidor a servidor, donde CORS no existe.
 * Mínimo privilegio: un permiso que nadie usa es solo una puerta más para dejar mal configurada.
 *
 * Sin el encabezado Access-Control-Allow-Origin, el navegador no deja que la página ajena lea la respuesta.
 * Incluye el que antes estaba permitido (el frontend local en el 3001): ya no hace falta.
 */
@ActiveProfiles("test")
class NoCrossOriginAccessIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:3001", "https://kiosco-la-madrina.vercel.app", "https://evil.example"})
    void preflight_FromAnyOrigin_GetsNoPermission(String origin) throws Exception {
        mockMvc.perform(options("/api/products")
                        .header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:3001", "https://evil.example"})
    void preflight_ToLogin_GetsNoPermission(String origin) throws Exception {
        mockMvc.perform(options("/api/auth/signin")
                        .header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void anAuthenticatedResponse_IsNotSharedWithAnotherOrigin() throws Exception {
        // Aunque el pedido llegue con credenciales válidas, la respuesta no autoriza a otro sitio a leerla.
        mockMvc.perform(get("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken())
                        .header(HttpHeaders.ORIGIN, "http://localhost:3001"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private String adminToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"AdminPass123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
