package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pedir algo que no existe es un error del cliente (404), no una falla del servidor (500).
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN") // "admin" existe: lo crea el bootstrap de tests
class NotFoundIntegrationTest extends AbstractIntegrationTest {

    private static final long MISSING_ID = 999_999;

    private static final String VALID_PRODUCT = """
            {"barcode":"nf-001","name":"Producto","price":100,"stockQuantity":1,"categoryId":%d}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMissingProduct_Returns404() throws Exception {
        mockMvc.perform(get("/api/products/{id}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el producto con id " + MISSING_ID));
    }

    @Test
    void updateMissingProduct_Returns404() throws Exception {
        mockMvc.perform(put("/api/products/{id}", MISSING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PRODUCT.formatted(MISSING_ID)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMissingProduct_Returns404() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", MISSING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProductWithMissingCategory_Returns404() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PRODUCT.formatted(MISSING_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró la categoría con id " + MISSING_ID));
    }

    @Test
    void updateMissingCategory_Returns404() throws Exception {
        mockMvc.perform(put("/api/categories/{id}", MISSING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bebidas\",\"description\":\"\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void saleWithMissingProduct_Returns404() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":%d,\"quantity\":1}]}".formatted(MISSING_ID)))
                .andExpect(status().isNotFound());
    }
}
