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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vender más de lo que hay es un conflicto con el inventario (409): el pedido es válido,
 * pero choca con el stock actual. Ni la venta ni el descuento de stock deben persistir.
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class InsufficientStockIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void sellingMoreThanAvailable_Returns409_AndKeepsStock() throws Exception {
        Product cola = productRepository.save(Product.builder()
                .barcode("is-" + UUID.randomUUID()).name("Coca-Cola")
                .price(new BigDecimal("1500")).stockQuantity(3).build());

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":%d,\"quantity\":5}]}".formatted(cola.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Stock insuficiente para Coca-Cola: pediste 5, quedan 3"));

        assertThat(productRepository.getRequiredById(cola.getId()).getStockQuantity()).isEqualTo(3);
    }
}
