package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Decisión de negocio: hay que reponer un producto cuando le quedan menos de 5 unidades, y está por agotarse con
 * menos de 3. Los umbrales viven solo en el backend (StockLevel): cada producto viaja con su nivel ya calculado
 * y el frontend solo lo pinta. Los productos desactivados no se reponen.
 */
@ActiveProfiles("test")
class LowStockIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithMockUser(username = "empleado", authorities = "EMPLOYEE")
    void lowStock_ListsActiveProductsWithLessThanFiveUnits_MostUrgentFirst() throws Exception {
        Product almostOut = product(1, true);
        Product running = product(4, true);
        Product enough = product(5, true);
        Product discontinued = product(0, false);

        String json = mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Integer> ids = JsonPath.read(json, "$[*].id");
        assertThat(ids)
                .contains(almostOut.getId().intValue(), running.getId().intValue())
                .doesNotContain(enough.getId().intValue(), discontinued.getId().intValue());
        assertThat(ids.indexOf(almostOut.getId().intValue())).isLessThan(ids.indexOf(running.getId().intValue()));

        List<Integer> stocks = JsonPath.read(json, "$[*].stockQuantity");
        assertThat(stocks).isSorted().allMatch(stock -> stock < 5);
    }

    @Test
    @WithMockUser(username = "empleado", authorities = "EMPLOYEE")
    void products_CarryTheirStockLevel_SoTheFrontendDoesNotRepeatTheThresholds() throws Exception {
        Product almostOut = product(2, true);
        Product running = product(3, true);
        Product enough = product(5, true);

        String json = mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(stockLevelOf(json, almostOut)).isEqualTo("CRITICAL");
        assertThat(stockLevelOf(json, running)).isEqualTo("LOW");
        assertThat(stockLevelOf(json, enough)).isEqualTo("OK");
    }

    @Test
    void lowStock_RequiresLogin() throws Exception {
        mockMvc.perform(get("/api/products/low-stock")).andExpect(status().isUnauthorized());
    }

    private static String stockLevelOf(String json, Product product) {
        List<String> levels = JsonPath.read(json, "$[?(@.id == " + product.getId() + ")].stockLevel");
        return levels.get(0);
    }

    private Product product(int stock, boolean active) {
        Product product = productRepository.save(Product.builder()
                .barcode("low-" + UUID.randomUUID()).name("Turrón").price(new BigDecimal("500"))
                .stockQuantity(stock).build());
        if (!active) {
            product.deactivate();
            product = productRepository.save(product);
        }
        return product;
    }
}
