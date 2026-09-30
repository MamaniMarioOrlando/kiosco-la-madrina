package devMario.example.kioscoLaMadrina.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del JWT (application.yaml → kiosco.app.*), leída una vez y tipada.
 *
 * @param jwtSecret       clave de firma en Base64 (variable de entorno JWT_SECRET_KEY)
 * @param jwtExpirationMs duración de cada token, en milisegundos
 */
@ConfigurationProperties(prefix = "kiosco.app")
public record JwtProperties(String jwtSecret, long jwtExpirationMs) {
}
