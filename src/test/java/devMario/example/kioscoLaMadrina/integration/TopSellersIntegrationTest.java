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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El nombre de un producto no es único (lo es el código de barras): el ranking de más vendidos
 * tiene que contar cada producto por separado aunque dos se llamen igual.
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class TopSellersIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleService saleService;

    @Test
    void productsWithTheSameName_AreRankedSeparately() throws Exception {
        // Cantidades muy por encima de las de otros tests, para que estos dos encabecen el ranking compartido.
        Product small = colaWithStock(5_000);
        Product large = colaWithStock(5_000);
        sell(small, 3_000);
        sell(large, 2_000);

        mockMvc.perform(get("/api/sales/top-sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(small.getId()))
                .andExpect(jsonPath("$[0].name").value("Coca-Cola"))
                .andExpect(jsonPath("$[0].quantity").value(3_000))
                .andExpect(jsonPath("$[1].productId").value(large.getId()))
                .andExpect(jsonPath("$[1].quantity").value(2_000));
    }

    private Product colaWithStock(int stock) {
        return productRepository.save(Product.builder()
                .barcode("top-" + UUID.randomUUID()).name("Coca-Cola").price(new BigDecimal("1500"))
                .stockQuantity(stock).build());
    }

    private void sell(Product product, int quantity) {
        saleService.createSale(new SaleRequestDTO(
                List.of(new SaleItemRequestDTO(product.getId(), quantity)), PaymentMethod.CASH), "admin");
    }
}
