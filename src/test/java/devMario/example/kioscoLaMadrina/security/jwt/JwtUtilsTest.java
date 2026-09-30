package devMario.example.kioscoLaMadrina.security.jwt;

import devMario.example.kioscoLaMadrina.security.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El token debe vencer exactamente después de la duración configurada.
 * Test unitario puro: construye las clases a mano, sin Spring.
 */
class JwtUtilsTest {

    @Test
    void tokenExpiresAfterTheConfiguredDuration() {
        assertThat(lifetimeOfTokenIssuedWith(Duration.ofHours(12))).isEqualTo(Duration.ofHours(12));
    }

    @Test
    void durationIsConfigurable() {
        assertThat(lifetimeOfTokenIssuedWith(Duration.ofMinutes(30))).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void generatedTokenIsValidAndIdentifiesTheUser() {
        JwtUtils jwtUtils = jwtUtilsWith(Duration.ofHours(12));

        String token = jwtUtils.generateJwtToken(authenticationFor("cajero"));

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("cajero");
    }

    private static Duration lifetimeOfTokenIssuedWith(Duration configured) {
        JwtProperties properties = new JwtProperties(randomSecret(), configured);
        JwtKeyProvider keyProvider = new JwtKeyProvider(properties);
        String token = new JwtUtils(keyProvider, properties).generateJwtToken(authenticationFor("cajero"));

        Claims claims = Jwts.parserBuilder().setSigningKey(keyProvider.getSigningKey()).build()
                .parseClaimsJws(token).getBody();
        return Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant());
    }

    private static JwtUtils jwtUtilsWith(Duration expiration) {
        JwtProperties properties = new JwtProperties(randomSecret(), expiration);
        return new JwtUtils(new JwtKeyProvider(properties), properties);
    }

    private static UsernamePasswordAuthenticationToken authenticationFor(String username) {
        UserDetailsImpl user = new UserDetailsImpl(1L, username, username + "@kiosco.test", null, "hash",
                List.of(new SimpleGrantedAuthority("EMPLOYEE")), true);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private static String randomSecret() {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }
}
