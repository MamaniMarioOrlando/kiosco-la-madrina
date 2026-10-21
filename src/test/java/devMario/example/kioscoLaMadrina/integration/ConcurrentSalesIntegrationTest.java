package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.dto.SaleItemRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.exception.InsufficientStockException;
import devMario.example.kioscoLaMadrina.model.PaymentMethod;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import devMario.example.kioscoLaMadrina.repository.SaleDetailRepository;
import devMario.example.kioscoLaMadrina.service.SaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Varias cajas vendiendo el mismo producto al mismo tiempo nunca pueden vender más de lo que hay.
 * Contra PostgreSQL real: la condición de carrera ocurre en la base, un mock no la reproduciría.
 */
@ActiveProfiles("test")
class ConcurrentSalesIntegrationTest extends AbstractIntegrationTest {

    private static final int STOCK = 5;
    private static final int BUYERS = 20;

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleDetailRepository saleDetailRepository;

    @Test
    void concurrentSales_NeverSellMoreThanTheAvailableStock() throws Exception {
        Product product = productRepository.save(Product.builder()
                .barcode("race-" + UUID.randomUUID()).name("Última Coca-Cola")
                .price(new BigDecimal("1500")).stockQuantity(STOCK).build());
        SaleRequestDTO buyOne = new SaleRequestDTO(
                List.of(new SaleItemRequestDTO(product.getId(), 1)), PaymentMethod.CASH);

        AtomicInteger sold = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        CountDownLatch startLine = new CountDownLatch(1);
        ExecutorService checkouts = Executors.newFixedThreadPool(BUYERS);
        try {
            List<Future<?>> attempts = IntStream.range(0, BUYERS)
                    .<Future<?>>mapToObj(i -> checkouts.submit(() -> {
                        startLine.await(); // todas las cajas arrancan a la vez
                        try {
                            saleService.createSale(buyOne, "admin");
                            sold.incrementAndGet();
                        } catch (InsufficientStockException e) {
                            rejected.incrementAndGet();
                        }
                        return null;
                    }))
                    .toList();

            startLine.countDown();
            for (Future<?> attempt : attempts) {
                attempt.get(60, TimeUnit.SECONDS); // propaga cualquier error inesperado
            }
        } finally {
            checkouts.shutdownNow();
        }

        assertThat(sold.get()).as("ventas aceptadas").isEqualTo(STOCK);
        assertThat(rejected.get()).as("ventas rechazadas por falta de stock").isEqualTo(BUYERS - STOCK);
        assertThat(productRepository.getRequiredById(product.getId()).getStockQuantity())
                .as("stock final").isZero();
        assertThat(saleDetailRepository.findAll()).filteredOn(d -> d.getProduct().getId().equals(product.getId()))
                .as("unidades registradas como vendidas").hasSize(STOCK);
    }
}
