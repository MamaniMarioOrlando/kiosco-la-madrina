package devMario.example.kioscoLaMadrina.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponseDTO(
        Long id,
        LocalDateTime dateTime,
        BigDecimal totalAmount,
        String username,
        List<SaleDetailDTO> details) {
}
