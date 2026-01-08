package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleItemRequestDTO(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity) {
}
