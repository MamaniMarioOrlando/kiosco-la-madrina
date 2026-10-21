package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.NotNull;

/** Activar o desactivar un recurso (usuario, producto…): body {"active": true|false}. */
public record UpdateStatusRequestDTO(@NotNull Boolean active) {
}
