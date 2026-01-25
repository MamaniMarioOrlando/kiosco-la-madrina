package devMario.example.kioscoLaMadrina.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CategoryDTO(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @NotBlank String name,
        String description) {
}
