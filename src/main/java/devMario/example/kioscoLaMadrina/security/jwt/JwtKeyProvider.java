package devMario.example.kioscoLaMadrina.security.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Set;

/**
 * Construye la clave de firma de los JWT una sola vez, al arrancar la aplicación.
 * Único lugar que sabe cómo se obtiene la clave a partir de la configuración.
 *
 * Fail fast: si la clave no es segura, la aplicación no arranca ({@link InsecureJwtSecretException}).
 * Es preferible un deploy fallido (la versión anterior sigue funcionando) a una aplicación en
 * producción donde cualquiera puede firmar tokens de ADMIN.
 * Los mensajes de error nunca incluyen la clave: terminan en los logs.
 */
@Component
public class JwtKeyProvider {

    /** HS256 necesita una clave de al menos el tamaño de su hash: 256 bits. */
    private static final int MIN_KEY_BYTES = 32;

    /**
     * Claves que circulan públicamente (tutoriales, ejemplos). Son largas, pero no secretas:
     * cualquiera que las conozca puede firmar tokens válidos.
     */
    private static final Set<String> PUBLICLY_KNOWN_SECRETS = Set.of(
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");

    private final Key signingKey;

    public JwtKeyProvider(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(decodeValidatedSecret(properties.jwtSecret()));
    }

    public Key getSigningKey() {
        return signingKey;
    }

    private static byte[] decodeValidatedSecret(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new InsecureJwtSecretException("JWT_SECRET_KEY no está definida.");
        }

        if (PUBLICLY_KNOWN_SECRETS.contains(secret.trim().toUpperCase())) {
            throw new InsecureJwtSecretException(
                    "JWT_SECRET_KEY es una clave de ejemplo pública: cualquiera podría falsificar tokens.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
        } catch (DecodingException e) {
            throw new InsecureJwtSecretException("JWT_SECRET_KEY no es Base64 válido.");
        }

        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new InsecureJwtSecretException("JWT_SECRET_KEY debe tener al menos 256 bits (tiene "
                    + keyBytes.length * 8 + ").");
        }

        return keyBytes;
    }
}
