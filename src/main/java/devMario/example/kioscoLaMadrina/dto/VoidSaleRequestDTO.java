package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo obligatorio: queda registrado para saber después por qué se anuló la venta. */
public record VoidSaleRequestDTO(
        @NotBlank(message = "Indicá el motivo de la anulación")
        @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
        String reason) {
}
