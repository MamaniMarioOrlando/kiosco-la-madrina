package devMario.example.kioscoLaMadrina.dto;

import devMario.example.kioscoLaMadrina.model.PaymentMethod;

import java.math.BigDecimal;

/** Lo vendido con un medio de pago: una fila del cierre de caja. */
public record PaymentMethodTotalDTO(
        PaymentMethod paymentMethod,
        BigDecimal total) {
}
