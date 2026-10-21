package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Decisión de negocio: un producto desactivado se puede reactivar (p. ej. un producto de temporada). */
@ActiveProfiles("test")
class ProductReactivationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void admin_SeesInactiveProducts_AndCanReactivateThem() throws Exception {
        Product seasonal = inactiveProduct();

        mockMvc.perform(get("/api/products/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)].active".formatted(seasonal.getId())).value(false));

        mockMvc.perform(patch("/api/products/{id}/status", seasonal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/products"))
                .andExpect(jsonPath("$[*].id", hasItem(seasonal.getId().intValue())));
        mockMvc.perform(get("/api/products/inactive"))
                .andExpect(jsonPath("$[*].id", not(hasItem(seasonal.getId().intValue()))));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void changingTheStatusOfAMissingProduct_Returns404() throws Exception {
        mockMvc.perform(patch("/api/products/{id}/status", 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "cajero", authorities = "EMPLOYEE")
    void employees_CannotSeeOrReactivateInactiveProducts() throws Exception {
        Product seasonal = inactiveProduct();

        mockMvc.perform(get("/api/products/inactive")).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/products/{id}/status", seasonal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true}"))
                .andExpect(status().isForbidden());
    }

    private Product inactiveProduct() {
        Product product = Product.builder()
                .barcode("re-" + UUID.randomUUID()).name("Pan dulce").price(new BigDecimal("5000"))
                .stockQuantity(10).build();
        product.deactivate();
        return productRepository.save(product);
    }
}
