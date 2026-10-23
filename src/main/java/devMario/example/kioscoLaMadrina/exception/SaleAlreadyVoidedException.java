package devMario.example.kioscoLaMadrina.exception;

/** Se intentó anular una venta que ya estaba anulada (por ejemplo, dos admins a la vez). */
public class SaleAlreadyVoidedException extends ConflictException {

    public SaleAlreadyVoidedException(Long saleId) {
        super("La venta #" + saleId + " ya está anulada");
    }
}
