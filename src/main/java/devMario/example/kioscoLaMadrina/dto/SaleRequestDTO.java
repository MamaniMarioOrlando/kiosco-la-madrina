package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import devMario.example.kioscoLaMadrina.model.PaymentMethod;

public record SaleRequestDTO(
        @NotEmpty List<@Valid SaleItemRequestDTO> items,
        PaymentMethod paymentMethod) {
}
