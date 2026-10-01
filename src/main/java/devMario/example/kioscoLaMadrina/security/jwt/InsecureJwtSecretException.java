package devMario.example.kioscoLaMadrina.security.jwt;

/**
 * La clave JWT configurada no es segura. Impide que la aplicación arranque.
 * {@link InsecureJwtSecretFailureAnalyzer} la convierte en un reporte legible al iniciar.
 * El mensaje describe el problema y nunca incluye la clave.
 */
public class InsecureJwtSecretException extends IllegalStateException {

    public InsecureJwtSecretException(String message) {
        super(message);
    }
}
