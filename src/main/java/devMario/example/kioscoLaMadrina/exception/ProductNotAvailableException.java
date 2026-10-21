package devMario.example.kioscoLaMadrina.exception;

/**
 * Se intentó vender un producto desactivado.
 * Hereda de {@link ConflictException}: el GlobalExceptionHandler ya la responde como 409.
 */
public class ProductNotAvailableException extends ConflictException {

    public ProductNotAvailableException(String productName) {
        super(productName + " no está disponible para la venta");
    }
}
