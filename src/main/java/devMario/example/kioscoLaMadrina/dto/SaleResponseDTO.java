package devMario.example.kioscoLaMadrina.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import devMario.example.kioscoLaMadrina.model.PaymentMethod;

public record SaleResponseDTO(
        Long id,
        Instant dateTime,
        BigDecimal totalAmount,
        String username,
        List<SaleDetailDTO> details,
        PaymentMethod paymentMethod) {
}
