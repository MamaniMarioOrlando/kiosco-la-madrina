package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.dto.PaymentMethodTotalDTO;
import devMario.example.kioscoLaMadrina.dto.TopSellerDTO;
import devMario.example.kioscoLaMadrina.exception.ResourceNotFoundException;
import devMario.example.kioscoLaMadrina.model.User;
import devMario.example.kioscoLaMadrina.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    /** Busca una venta que debe existir; si no existe, la API responde 404. */
    default Sale getRequiredById(Long id) {
        return findById(id).orElseThrow(
                () -> new ResourceNotFoundException("No se encontró la venta con id " + id));
    }

    /**
     * Marca la venta como anulada solo si todavía no lo estaba, en una sola operación atómica:
     * si dos admins anulan a la vez, uno solo lo logra y el stock se devuelve una vez.
     * Devuelve 1 si la anuló y 0 si ya estaba anulada.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Sale s SET s.voidedAt = :at, s.voidedBy = :by, s.voidReason = :reason "
            + "WHERE s.id = :id AND s.voidedAt IS NULL")
    int markVoidedIfActive(@Param("id") Long id, @Param("at") Instant at,
                           @Param("by") User by, @Param("reason") String reason);
    // Rango semiabierto [from, to): el instante "to" es el comienzo del día siguiente y no se incluye.
    Page<Sale> findByDateTimeGreaterThanEqualAndDateTimeLessThan(Instant from, Instant to, Pageable pageable);

    @Query("SELECT new devMario.example.kioscoLaMadrina.dto.PaymentMethodTotalDTO(s.paymentMethod, SUM(s.totalAmount)) "
            + "FROM Sale s "
            + "WHERE s.dateTime >= :from AND s.dateTime < :to AND s.voidedAt IS NULL GROUP BY s.paymentMethod")
    List<PaymentMethodTotalDTO> getSalesSummaryByPaymentMethodBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT new devMario.example.kioscoLaMadrina.dto.PaymentMethodTotalDTO(s.paymentMethod, SUM(s.totalAmount)) "
            + "FROM Sale s WHERE s.voidedAt IS NULL GROUP BY s.paymentMethod")
    List<PaymentMethodTotalDTO> getSalesSummaryByPaymentMethod();
    // Las ventas anuladas no cuentan en ningún total ni ranking: siguen en el historial, pero no ocurrieron.

    // Agrupa por id (el nombre no es único). El id también desempata, para que el orden sea estable.
    @Query("""
            SELECT new devMario.example.kioscoLaMadrina.dto.TopSellerDTO(p.id, p.name, SUM(d.quantity))
            FROM SaleDetail d JOIN d.product p
            WHERE d.sale.voidedAt IS NULL
            GROUP BY p.id, p.name
            ORDER BY SUM(d.quantity) DESC, p.id""")
    List<TopSellerDTO> findTopSellers(Pageable pageable);
}
