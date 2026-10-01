package devMario.example.kioscoLaMadrina.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Políticas de rate limiting: todas en un solo lugar.
 * Los métodos estáticos crean cada registro y los usan tanto los @Bean como los tests,
 * así un test siempre verifica la política real y no una copia.
 */
@Configuration
public class RateLimitConfig {

    /** Más que suficiente para un kiosco, que pide decenas de veces por minuto. */
    private static final long MAX_TRACKED_KEYS = 50_000;

    /** Ventana del bloqueo de login (decisión de negocio). */
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);

    /** Límite general de la API: 300 pedidos por minuto por IP. */
    @Bean
    public BucketRegistry apiRateLimits() {
        return new BucketRegistry(Bandwidth.builder()
                .capacity(300)
                .refillGreedy(300, Duration.ofMinutes(1))
                .build(), MAX_TRACKED_KEYS);
    }

    @Bean
    public BucketRegistry loginFailuresPerAccount() {
        return loginFailuresPerAccountRegistry();
    }

    @Bean
    public BucketRegistry loginFailuresPerIp() {
        return loginFailuresPerIpRegistry();
    }

    /**
     * 5 intentos fallidos cada 15 minutos por cuenta e IP (decisión de negocio).
     * refillIntervally devuelve los 5 juntos al cumplirse la ventana: "esperá N minutos".
     */
    static BucketRegistry loginFailuresPerAccountRegistry() {
        return new BucketRegistry(Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, LOGIN_WINDOW)
                .build(), MAX_TRACKED_KEYS);
    }

    /** 20 fallos cada 15 minutos por IP, sumando todas las cuentas: frena el "password spraying". */
    static BucketRegistry loginFailuresPerIpRegistry() {
        return new BucketRegistry(Bandwidth.builder()
                .capacity(20)
                .refillIntervally(20, LOGIN_WINDOW)
                .build(), MAX_TRACKED_KEYS);
    }
}
