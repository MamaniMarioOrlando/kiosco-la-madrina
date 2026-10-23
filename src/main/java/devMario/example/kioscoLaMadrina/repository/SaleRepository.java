package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    // Rango semiabierto [from, to): el instante "to" es el comienzo del día siguiente y no se incluye.
    Page<Sale> findByDateTimeGreaterThanEqualAndDateTimeLessThan(Instant from, Instant to, Pageable pageable);

    @Query("SELECT s.paymentMethod, SUM(s.totalAmount) FROM Sale s WHERE s.dateTime >= :from AND s.dateTime < :to GROUP BY s.paymentMethod")
    List<Object[]> getSalesSummaryByPaymentMethodBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT s.paymentMethod, SUM(s.totalAmount) FROM Sale s GROUP BY s.paymentMethod")
    List<Object[]> getSalesSummaryByPaymentMethod();
    @Query("SELECT d.product.name, SUM(d.quantity) FROM SaleDetail d GROUP BY d.product.name ORDER BY SUM(d.quantity) DESC")
    List<Object[]> getTopSellersQuery(Pageable pageable);
}
