package devMario.example.kioscoLaMadrina.dto;

import java.math.BigDecimal;

public record DailySalesSummaryDTO(
        BigDecimal cashTotal,
        BigDecimal mpTotal,
        BigDecimal grandTotal
) {
}
