package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Una venta anulada sigue visible en el historial, pero no suma en la caja ni en las estadísticas. */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class VoidedSalesTotalsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleService saleService;

    @Test
    void aVoidedSale_IsListedButNotCounted() throws Exception {
        BigDecimal todayBefore = grandTotal("/api/sales/summary?date=" + today());
        BigDecimal allTimeBefore = grandTotal("/api/sales/summary");
        // Cantidad muy por encima de las de otros tests: sin anular, encabezaría el ranking.
        Product product = productWithStock(50_000);
        long saleId = sell(product, 40_000);

        saleService.voidSale(saleId, "Venta de prueba", "admin");

        assertThat(grandTotal("/api/sales/summary?date=" + today())).isEqualByComparingTo(todayBefore);
        assertThat(grandTotal("/api/sales/summary")).isEqualByComparingTo(allTimeBefore);
        mockMvc.perform(get("/api/sales/top-sellers"))
                .andExpect(jsonPath("$[*].productId", not(hasItem(product.getId().intValue()))));
        mockMvc.perform(get("/api/sales").param("date", today()).param("size", "100"))
                .andExpect(jsonPath("$.content[*].id", hasItem((int) saleId)));
    }

    private BigDecimal grandTotal(String url) throws Exception {
        String json = mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString();
        return new BigDecimal(JsonPath.read(json, "$.grandTotal").toString());
    }

    private String today() {
        return java.time.LocalDate.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).toString();
    }

    private Product productWithStock(int stock) {
        return productRepository.save(Product.builder()
                .barcode("vt-" + UUID.randomUUID()).name("Chicle").price(new BigDecimal("100"))
                .stockQuantity(stock).build());
    }

    private long sell(Product product, int quantity) {
        return saleService.createSale(new SaleRequestDTO(
                List.of(new SaleItemRequestDTO(product.getId(), quantity)), PaymentMethod.CASH), "admin").id();
    }
}
