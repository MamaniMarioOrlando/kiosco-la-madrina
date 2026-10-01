package devMario.example.kioscoLaMadrina.security.ratelimit;

import java.time.Duration;

/** Valor del encabezado HTTP Retry-After: segundos enteros, redondeados hacia arriba, mínimo 1. */
public final class RetryAfter {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private RetryAfter() {
    }

    public static long seconds(Duration wait) {
        return Math.max(1, Math.ceilDiv(wait.toNanos(), NANOS_PER_SECOND));
    }
}
