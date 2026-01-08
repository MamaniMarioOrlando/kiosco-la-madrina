package devMario.example.kioscoLaMadrina.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record ProductDTO(
        Long id,
        @NotBlank String barcode,
        @NotBlank String name,
        @NotNull @Positive BigDecimal price,
        @NotNull @Min(0) Integer stockQuantity,
        Long categoryId,
        String categoryName) {
}
