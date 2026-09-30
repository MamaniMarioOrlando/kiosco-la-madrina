package devMario.example.kioscoLaMadrina.security.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * Construye la clave de firma de los JWT una sola vez, al arrancar la aplicación.
 * Único lugar que sabe cómo se obtiene la clave a partir de la configuración.
 */
@Component
public class JwtKeyProvider {

    private final Key signingKey;

    public JwtKeyProvider(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.jwtSecret()));
    }

    public Key getSigningKey() {
        return signingKey;
    }
}
