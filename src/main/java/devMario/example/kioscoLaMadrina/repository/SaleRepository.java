package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Page<Sale> findByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT s.paymentMethod, SUM(s.totalAmount) FROM Sale s WHERE s.dateTime BETWEEN :startDate AND :endDate GROUP BY s.paymentMethod")
    List<Object[]> getSalesSummaryByPaymentMethodAndDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    @Query("SELECT d.product.name, SUM(d.quantity) FROM SaleDetail d GROUP BY d.product.name ORDER BY SUM(d.quantity) DESC")
    List<Object[]> getTopSellersQuery(Pageable pageable);
}
