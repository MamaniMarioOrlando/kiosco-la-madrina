package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = ProductController.class)
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
