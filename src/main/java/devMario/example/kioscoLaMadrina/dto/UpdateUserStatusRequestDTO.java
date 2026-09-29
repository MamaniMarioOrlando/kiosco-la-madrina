package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequestDTO(@NotNull Boolean active) {
}
