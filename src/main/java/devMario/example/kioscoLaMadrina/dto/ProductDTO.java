package devMario.example.kioscoLaMadrina.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductDTO(
                @Schema(accessMode = Schema.AccessMode.READ_ONLY) Long id,
                @NotBlank String barcode,
                @NotBlank String name,
                @NotNull @Positive BigDecimal price,
                @NotNull @Min(0) Integer stockQuantity,
                Long categoryId,
                String categoryName) {
}
