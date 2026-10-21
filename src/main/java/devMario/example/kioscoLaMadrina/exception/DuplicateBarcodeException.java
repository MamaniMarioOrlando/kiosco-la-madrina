package devMario.example.kioscoLaMadrina.exception;

import devMario.example.kioscoLaMadrina.model.Product;

/**
 * El código de barras ya pertenece a otro producto. Si ese producto está inactivo,
 * el mensaje sugiere reactivarlo. Hereda de {@link ConflictException} (409).
 */
public class DuplicateBarcodeException extends ConflictException {

    public DuplicateBarcodeException(Product existing) {
        super(existing.isActive()
                ? "Ya existe un producto con el código %s: %s".formatted(existing.getBarcode(), existing.getName())
                : "Ya existe un producto inactivo con el código %s: %s. Reactivalo en lugar de crear uno nuevo"
                        .formatted(existing.getBarcode(), existing.getName()));
    }
}
