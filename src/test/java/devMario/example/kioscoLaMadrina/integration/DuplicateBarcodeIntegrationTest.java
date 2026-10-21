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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El código de barras identifica un producto: no puede repetirse. Si lo tiene un producto inactivo,
 * el mensaje sugiere reactivarlo (con el borrado lógico, el producto "borrado" conserva su código).
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class DuplicateBarcodeIntegrationTest extends AbstractIntegrationTest {

    private static final String PRODUCT_JSON = """
            {"barcode":"%s","name":"Coca-Cola 500ml","price":1500,"stockQuantity":10}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void creatingAProductWithAnExistingBarcode_Returns409() throws Exception {
        Product existing = newProduct("Coca-Cola", true);

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON.formatted(existing.getBarcode())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Ya existe un producto con el código " + existing.getBarcode() + ": Coca-Cola"));
    }

    @Test
    void barcodeOfAnInactiveProduct_SuggestsReactivatingIt() throws Exception {
        Product discontinued = newProduct("Pan dulce", false);

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON.formatted(discontinued.getBarcode())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Ya existe un producto inactivo con el código " + discontinued.getBarcode()
                                + ": Pan dulce. Reactivalo en lugar de crear uno nuevo"));
    }

    @Test
    void updatingAProductToAnotherProductsBarcode_Returns409() throws Exception {
        Product cola = newProduct("Coca-Cola", true);
        Product sprite = newProduct("Sprite", true);

        mockMvc.perform(put("/api/products/{id}", sprite.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON.formatted(cola.getBarcode())))
                .andExpect(status().isConflict());
    }

    @Test
    void updatingAProductKeepingItsOwnBarcode_Succeeds() throws Exception {
        Product cola = newProduct("Coca-Cola", true);

        mockMvc.perform(put("/api/products/{id}", cola.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON.formatted(cola.getBarcode())))
                .andExpect(status().isOk());
    }

    private Product newProduct(String name, boolean active) {
        Product product = Product.builder()
                .barcode("bc-" + UUID.randomUUID()).name(name).price(new BigDecimal("1500")).stockQuantity(10).build();
        if (!active) {
            product.deactivate();
        }
        return productRepository.save(product);
    }
}
