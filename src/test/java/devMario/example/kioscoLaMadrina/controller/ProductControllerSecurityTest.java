package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.service.ProductService;
import org.junit.jupiter.api.Test;
import devMario.example.kioscoLaMadrina.security.SecurityErrorResponder;
import devMario.example.kioscoLaMadrina.security.ratelimit.RateLimitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

// Sin el usuario en memoria que Spring Boot crea por defecto: su condición se evalúa antes de registrar
// el mock de UserDetailsServiceImpl, así que quedarían dos UserDetailsService (y un WARN al arrancar).
@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
// El filtro de rate limiting (capa web) necesita sus políticas y el formato JSON de errores.
@Import({RateLimitConfig.class, SecurityErrorResponder.class})
@EnableMethodSecurity
public class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    // Falsificamos las dependencias del AuthTokenFilter.
    // ¡IMPORTANTE! NO falsificamos el AuthTokenFilter en sí mismo para no romper la
    // cadena de filtros de la solicitud.
    @MockitoBean
    private devMario.example.kioscoLaMadrina.security.jwt.JwtUtils jwtUtils;

    @MockitoBean
    private devMario.example.kioscoLaMadrina.security.services.UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(authorities = "EMPLOYEE") // Disfrazamos al "Navegador" de Empleado
    void testDeleteProduct_AsEmployee_Returns403Forbidden() throws Exception {

        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN") // Disfrazamos al "Navegador" de Administrador Supremo
    void testDeleteProduct_AsAdmin_Returns200Ok() throws Exception {

        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isOk());
    }
}
