package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.dto.PageResponseDTO;
import devMario.example.kioscoLaMadrina.dto.SaleItemRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.model.PaymentMethod;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import devMario.example.kioscoLaMadrina.service.SaleService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Problema N+1: listar N ventas no debe costar 1 consulta por venta (sus ítems) y otra por producto.
 * La cantidad de consultas tiene que ser fija, sin importar cuántas ventas ni productos haya en la página.
 */
@ActiveProfiles("test")
class SalesHistoryQueryCountIntegrationTest extends AbstractIntegrationTest {

    private static final int PAGE_SIZE = 8;

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void listingAPageOfSales_TakesAFixedNumberOfQueries() {
        for (int i = 0; i < PAGE_SIZE; i++) {
            sell(newProduct(), 1);
        }
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        statistics.setStatisticsEnabled(true);

        PageResponseDTO<SaleResponseDTO> page;
        try {
            page = saleService.getSales(null, 0, PAGE_SIZE);
        } finally {
            statistics.setStatisticsEnabled(false);
        }

        assertThat(page.content()).hasSize(PAGE_SIZE);
        // Ventas + total de páginas + ítems + productos + usuarios: una consulta por tipo de dato, no por fila.
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(5);
    }

    private Product newProduct() {
        return productRepository.save(Product.builder()
                .barcode("history-" + UUID.randomUUID()).name("Chicle").price(new BigDecimal("300"))
                .stockQuantity(10).build());
    }

    private void sell(Product product, int quantity) {
        saleService.createSale(new SaleRequestDTO(
                List.of(new SaleItemRequestDTO(product.getId(), quantity)), PaymentMethod.CASH), "admin");
    }
}
