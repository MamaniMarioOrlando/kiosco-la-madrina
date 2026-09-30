package devMario.example.kioscoLaMadrina.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuración del JWT (application.yaml → kiosco.app.*), leída una vez y tipada.
 *
 * @param jwtSecret     clave de firma en Base64 (variable de entorno JWT_SECRET_KEY)
 * @param jwtExpiration duración de cada sesión (variable JWT_EXPIRATION, p. ej. "12h" o "30m")
 */
@ConfigurationProperties(prefix = "kiosco.app")
public record JwtProperties(String jwtSecret, Duration jwtExpiration) {
}
