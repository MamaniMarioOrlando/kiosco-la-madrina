package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;
import java.util.List;

public record SaleRequestDTO(
        @NotEmpty List<@Valid SaleItemRequestDTO> items) {
}
