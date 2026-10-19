package devMario.example.kioscoLaMadrina.exception;

/**
 * Se pidió vender más unidades de las que hay en stock.
 * Hereda de {@link ConflictException}: el GlobalExceptionHandler ya la responde como 409.
 */
public class InsufficientStockException extends ConflictException {

    public InsufficientStockException(String productName, int requested, int available) {
        super("Stock insuficiente para %s: pediste %d, quedan %d".formatted(productName, requested, available));
    }
}
