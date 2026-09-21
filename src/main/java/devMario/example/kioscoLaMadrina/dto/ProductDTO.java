package devMario.example.kioscoLaMadrina.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductDTO(
                @Schema(accessMode = Schema.AccessMode.READ_ONLY) Long id,
                @NotBlank String barcode,
                @NotBlank String name,
                @NotNull @Min(value = 0, message = "El precio no puede ser negativo") @Max(value = 10000000, message = "El precio no puede superar los 10 millones") BigDecimal price,
                @NotNull @Min(value = 0, message = "El stock no puede ser negativo") @Max(value = 100000, message = "El stock no puede superar las 100.000 unidades") Integer stockQuantity,
                Long categoryId,
                String categoryName) {
}
