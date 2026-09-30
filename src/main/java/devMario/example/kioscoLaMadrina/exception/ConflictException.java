package devMario.example.kioscoLaMadrina.exception;

/**
 * La operación choca con el estado actual del recurso (p. ej. un username ya tomado).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
