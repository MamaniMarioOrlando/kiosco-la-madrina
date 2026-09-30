package devMario.example.kioscoLaMadrina.security.jwt;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import devMario.example.kioscoLaMadrina.security.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
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

    // Captura en memoria los logs de JwtUtils para verificar su nivel sin depender de la consola.
    private final Logger jwtUtilsLogger = (Logger) LoggerFactory.getLogger(JwtUtils.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();

    @BeforeEach
    void captureLogs() {
        jwtUtilsLogger.setLevel(Level.DEBUG);
        logs.start();
        jwtUtilsLogger.addAppender(logs);
    }

    @AfterEach
    void releaseLogs() {
        jwtUtilsLogger.detachAppender(logs);
        jwtUtilsLogger.setLevel(null);
    }

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

    @Test
    void expiredTokenIsRejected() {
        // Duración negativa: el token nace vencido (evita un Thread.sleep lento e inestable en el test).
        JwtUtils jwtUtils = jwtUtilsWith(Duration.ofMinutes(-1));

        String token = jwtUtils.generateJwtToken(authenticationFor("cajero"));

        assertThat(jwtUtils.validateJwtToken(token)).isFalse();
    }

    @Test
    void expiredToken_IsLoggedAtDebug_BecauseSessionsExpireEveryDay() {
        JwtUtils jwtUtils = jwtUtilsWith(Duration.ofMinutes(-1));

        jwtUtils.validateJwtToken(jwtUtils.generateJwtToken(authenticationFor("cajero")));

        assertThat(logs.list).extracting(ILoggingEvent::getLevel).containsOnly(Level.DEBUG);
    }

    @Test
    void tamperedToken_IsRejectedAndLoggedAsWarning() {
        JwtUtils jwtUtils = jwtUtilsWith(Duration.ofHours(12));
        String token = jwtUtils.generateJwtToken(authenticationFor("cajero"));

        assertThat(jwtUtils.validateJwtToken(tamperSignature(token))).isFalse();
        assertThat(logs.list).extracting(ILoggingEvent::getLevel).containsOnly(Level.WARN);
    }

    @Test
    void tokenSignedWithAnotherKey_IsRejectedAndLoggedAsWarning() {
        String forged = jwtUtilsWith(Duration.ofHours(12)).generateJwtToken(authenticationFor("admin"));

        assertThat(jwtUtilsWith(Duration.ofHours(12)).validateJwtToken(forged)).isFalse();
        assertThat(logs.list).extracting(ILoggingEvent::getLevel).containsOnly(Level.WARN);
    }

    @Test
    void malformedToken_IsRejectedAndLoggedAsWarning() {
        assertThat(jwtUtilsWith(Duration.ofHours(12)).validateJwtToken("esto.no.es-un-jwt")).isFalse();
        assertThat(logs.list).extracting(ILoggingEvent::getLevel).containsOnly(Level.WARN);
    }

    @Test
    void emptyToken_IsRejectedAndLoggedAtDebug() {
        assertThat(jwtUtilsWith(Duration.ofHours(12)).validateJwtToken("")).isFalse();
        assertThat(logs.list).extracting(ILoggingEvent::getLevel).containsOnly(Level.DEBUG);
    }

    /** Cambia el último carácter de la firma: el token parece válido pero no fue firmado con nuestra clave. */
    private static String tamperSignature(String token) {
        char last = token.charAt(token.length() - 1);
        return token.substring(0, token.length() - 1) + (last == 'A' ? 'B' : 'A');
    }

    private static Duration lifetimeOfTokenIssuedWith(Duration configured) {
        JwtProperties properties = new JwtProperties(randomSecret(), configured);
        JwtKeyProvider keyProvider = new JwtKeyProvider(properties);
        String token = new JwtUtils(keyProvider, properties).generateJwtToken(authenticationFor("cajero"));

        Claims claims = Jwts.parser().verifyWith(keyProvider.getSigningKey()).build()
                .parseSignedClaims(token).getPayload();
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
