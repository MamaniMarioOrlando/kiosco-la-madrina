package devMario.example.kioscoLaMadrina.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/** Políticas de rate limiting: todas en un solo lugar. */
@Configuration
public class RateLimitConfig {

    /** Más que suficiente para un kiosco, que pide decenas de veces por minuto. */
    private static final long MAX_TRACKED_KEYS = 50_000;

    /** Límite general de la API: 300 pedidos por minuto por IP. */
    @Bean
    public BucketRegistry apiRateLimits() {
        return new BucketRegistry(Bandwidth.builder()
                .capacity(300)
                .refillGreedy(300, Duration.ofMinutes(1))
                .build(), MAX_TRACKED_KEYS);
    }
}
