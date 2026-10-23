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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Decisiones de negocio: solo un ADMIN anula, solo ventas del día (un día cerrado no cambia),
 * con motivo obligatorio, y la mercadería vuelve al stock. La venta no se borra: queda en el historial.
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class SaleVoidingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleService saleService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void admin_VoidsTodaysSale_RestoresStock_AndRecordsWhoAndWhy() throws Exception {
        Product product = productWithStock(10);
        long saleId = sell(product, 3);

        voidSale(saleId, "Cobré de más")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voidedAt").isNotEmpty())
                .andExpect(jsonPath("$.voidedBy").value("admin"))
                .andExpect(jsonPath("$.voidReason").value("Cobré de más"));

        assertThat(stockOf(product)).isEqualTo(10);
    }

    @Test
    void voidingTheSameSaleTwice_Returns409_AndRestoresStockOnlyOnce() throws Exception {
        Product product = productWithStock(10);
        long saleId = sell(product, 3);
        voidSale(saleId, "Error de cobro").andExpect(status().isOk());

        voidSale(saleId, "Error de cobro")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La venta #" + saleId + " ya está anulada"));

        assertThat(stockOf(product)).isEqualTo(10);
    }

    @Test
    void aSaleFromAnotherDay_CannotBeVoided() throws Exception {
        Product product = productWithStock(10);
        long saleId = sell(product, 3);
        jdbc.update("UPDATE sales SET date_time = date_time - interval '2 days' WHERE id = ?", saleId);

        voidSale(saleId, "Me olvidé de anularla")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Solo se pueden anular ventas del día: la venta #" + saleId + " pertenece a una caja ya cerrada"));

        assertThat(stockOf(product)).isEqualTo(7);
    }

    @Test
    void theReasonIsRequired() throws Exception {
        long saleId = sell(productWithStock(10), 1);

        voidSale(saleId, "   ").andExpect(status().isBadRequest());
    }

    @Test
    void voidingAMissingSale_Returns404() throws Exception {
        voidSale(999_999L, "No existe").andExpect(status().isNotFound());
    }

    @Test
    void employees_CannotVoidSales() throws Exception {
        long saleId = sell(productWithStock(10), 1);

        mockMvc.perform(post("/api/sales/{id}/void", saleId)
                        .with(user("cajero").authorities(() -> "EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Me equivoqué\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void concurrentVoids_RestoreTheStockOnlyOnce() throws Exception {
        Product product = productWithStock(10);
        long saleId = sell(product, 3);

        CountDownLatch startLine = new CountDownLatch(1);
        ExecutorService admins = Executors.newFixedThreadPool(5);
        try {
            List<Future<Integer>> attempts = IntStream.range(0, 5)
                    .mapToObj(i -> admins.submit(() -> {
                        startLine.await(); // todos los admins tocan "Anular" a la vez
                        return voidSale(saleId, "Doble clic").andReturn().getResponse().getStatus();
                    }))
                    .toList();
            startLine.countDown();

            List<Integer> statuses = attempts.stream().map(attempt -> {
                try {
                    return attempt.get(60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).toList();

            assertThat(statuses).filteredOn(s -> s == 200).hasSize(1);
            assertThat(statuses).filteredOn(s -> s == 409).hasSize(4);
        } finally {
            admins.shutdownNow();
        }
        assertThat(stockOf(product)).as("stock final").isEqualTo(10);
    }

    private ResultActions voidSale(long saleId, String reason) throws Exception {
        return mockMvc.perform(post("/api/sales/{id}/void", saleId)
                .with(user("admin").authorities(() -> "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"" + reason + "\"}"));
    }

    private Product productWithStock(int stock) {
        return productRepository.save(Product.builder()
                .barcode("void-" + UUID.randomUUID()).name("Alfajor").price(new BigDecimal("800"))
                .stockQuantity(stock).build());
    }

    private long sell(Product product, int quantity) {
        return saleService.createSale(new SaleRequestDTO(
                List.of(new SaleItemRequestDTO(product.getId(), quantity)), PaymentMethod.CASH), "admin").id();
    }

    private int stockOf(Product product) {
        return productRepository.getRequiredById(product.getId()).getStockQuantity();
    }
}
