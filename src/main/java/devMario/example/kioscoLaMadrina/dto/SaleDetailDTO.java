package devMario.example.kioscoLaMadrina.dto;

import java.math.BigDecimal;

public record SaleDetailDTO(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
