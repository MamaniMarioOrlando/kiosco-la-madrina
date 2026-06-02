package devMario.example.kioscoLaMadrina.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import devMario.example.kioscoLaMadrina.model.PaymentMethod;

public record SaleResponseDTO(
        Long id,
        LocalDateTime dateTime,
        BigDecimal totalAmount,
        String username,
        List<SaleDetailDTO> details,
        PaymentMethod paymentMethod) {
}
