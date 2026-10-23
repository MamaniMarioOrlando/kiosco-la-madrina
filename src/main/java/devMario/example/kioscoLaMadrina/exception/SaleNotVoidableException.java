package devMario.example.kioscoLaMadrina.exception;

/** Solo se anulan ventas del día: un día cuya caja ya se cerró no cambia después. */
public class SaleNotVoidableException extends ConflictException {

    public SaleNotVoidableException(Long saleId) {
        super("Solo se pueden anular ventas del día: la venta #" + saleId + " pertenece a una caja ya cerrada");
    }
}
