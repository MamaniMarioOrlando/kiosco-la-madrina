package devMario.example.kioscoLaMadrina.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleDetail> details = new ArrayList<>();

    /**
     * Agrega un ítem a la venta y actualiza el total. La venta mantiene su propia regla
     * (total = suma de subtotales), así nadie puede agregar un ítem y olvidarse del total.
     * Guarda el precio del momento: si el producto cambia de precio después, la venta no cambia.
     */
    public void addItem(Product product, int quantity) {
        BigDecimal unitPrice = product.getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        details.add(SaleDetail.builder()
                .sale(this)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build());
        totalAmount = totalAmount.add(subtotal);
    }
}
