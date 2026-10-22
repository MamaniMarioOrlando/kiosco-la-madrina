package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.model.PaymentMethod;
import devMario.example.kioscoLaMadrina.model.Sale;
import devMario.example.kioscoLaMadrina.repository.SaleRepository;
import devMario.example.kioscoLaMadrina.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Decisión de negocio: el día de ventas va de 00:00 a 23:59 en hora de Argentina, sin importar
 * en qué zona horaria corra el servidor (Railway corre en UTC, 3 horas adelante).
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class SalesBusinessDayIntegrationTest extends AbstractIntegrationTest {

    // 23:30 del 9 de marzo en Argentina = 02:30 del 10 de marzo en UTC. Fecha lejana: ningún otro test vende ese día.
    private static final Instant LATE_NIGHT_SALE = Instant.parse("2030-03-10T02:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void aLateNightSale_CountsForTheArgentineDay() throws Exception {
        Sale sale = saleRepository.save(Sale.builder()
                .dateTime(LATE_NIGHT_SALE)
                .totalAmount(new BigDecimal("2500.00"))
                .paymentMethod(PaymentMethod.CASH)
                .user(userRepository.getRequiredByUsername("admin"))
                .build());

        mockMvc.perform(get("/api/sales/summary").param("date", "2030-03-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grandTotal").value(2500.00));
        mockMvc.perform(get("/api/sales/summary").param("date", "2030-03-10"))
                .andExpect(jsonPath("$.grandTotal").value(0));

        mockMvc.perform(get("/api/sales").param("date", "2030-03-09"))
                .andExpect(jsonPath("$.content[*].id", hasItem(sale.getId().intValue())))
                .andExpect(jsonPath("$.content[0].dateTime").value("2030-03-10T02:30:00Z"));
        mockMvc.perform(get("/api/sales").param("date", "2030-03-10"))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(sale.getId().intValue()))));
    }
}
