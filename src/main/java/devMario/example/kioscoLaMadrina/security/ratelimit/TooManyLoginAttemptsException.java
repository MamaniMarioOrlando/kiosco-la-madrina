package devMario.example.kioscoLaMadrina.security.ratelimit;

import java.time.Duration;

/** Demasiados intentos fallidos de login: se rechaza el intento sin verificar la contraseña. */
public class TooManyLoginAttemptsException extends RuntimeException {

    private final Duration retryAfter;

    public TooManyLoginAttemptsException(Duration retryAfter) {
        super("Demasiados intentos fallidos de inicio de sesión");
        this.retryAfter = retryAfter;
    }

    /** Cuánto falta para poder volver a intentar. */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
