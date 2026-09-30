package devMario.example.kioscoLaMadrina.security.jwt;

import devMario.example.kioscoLaMadrina.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtKeyProvider keyProvider;
    private final JwtProperties properties;

    // Inmutable y thread-safe: se construye una vez y se comparte entre todos los requests.
    private final JwtParser parser;

    public JwtUtils(JwtKeyProvider keyProvider, JwtProperties properties) {
        this.keyProvider = keyProvider;
        this.properties = properties;
        this.parser = Jwts.parser().verifyWith(keyProvider.getSigningKey()).build();
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.jwtExpiration())))
                // Algoritmo explícito: si se deja que jjwt lo deduzca del largo de la clave,
                // una clave más larga cambiaría el algoritmo sin que nadie lo decida.
                .signWith(keyProvider.getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            parser.parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
