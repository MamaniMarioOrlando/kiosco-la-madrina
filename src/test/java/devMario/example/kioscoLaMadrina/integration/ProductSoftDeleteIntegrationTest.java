package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.dto.SaleItemRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.model.PaymentMethod;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import devMario.example.kioscoLaMadrina.service.SaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Borrado lógico: "borrar" un producto lo desactiva. Desaparece del catálogo y de la venta,
 * pero sigue existiendo para que el historial de ventas no pierda a qué producto corresponde.
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class ProductSoftDeleteIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleService saleService;

    @Test
    void deletingAProductThatWasAlreadySold_DeactivatesItAndKeepsHistory() throws Exception {
        Product sold = newProduct();
        saleService.createSale(new SaleRequestDTO(
                List.of(new SaleItemRequestDTO(sold.getId(), 1)), PaymentMethod.CASH), "admin");

        mockMvc.perform(delete("/api/products/{id}", sold.getId()))
                .andExpect(status().isOk());

        Product stored = productRepository.getRequiredById(sold.getId());
        assertThat(stored.isActive()).isFalse();
    }

    @Test
    void deactivatedProducts_AreNotListedInTheCatalog() throws Exception {
        Product active = newProduct();
        Product removed = newProduct();

        mockMvc.perform(delete("/api/products/{id}", removed.getId())).andExpect(status().isOk());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(active.getId().intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(removed.getId().intValue()))));
    }

    private Product newProduct() {
        return productRepository.save(Product.builder()
                .barcode("sd-" + UUID.randomUUID()).name("Producto").price(new BigDecimal("100"))
                .stockQuantity(5).build());
    }
}
