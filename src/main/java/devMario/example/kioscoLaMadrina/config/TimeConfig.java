package devMario.example.kioscoLaMadrina.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * El tiempo se guarda como instantes en UTC; la zona del negocio solo decide "qué día es".
 * Así el resultado no depende de la zona horaria del servidor (Railway corre en UTC).
 */
@Configuration
public class TimeConfig {

    /** Inyectar el reloj (en vez de llamar a Instant.now()) permite fijar la hora en los tests. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ZoneId businessZone(@Value("${kiosco.app.timeZone}") String timeZone) {
        return ZoneId.of(timeZone);
    }
}
