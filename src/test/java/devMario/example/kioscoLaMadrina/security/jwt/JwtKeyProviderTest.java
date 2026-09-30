package devMario.example.kioscoLaMadrina.security.jwt;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Fail fast: la aplicación no debe arrancar con una clave JWT ausente, inválida, corta o pública.
 * Test unitario puro: no levanta Spring ni la base de datos.
 */
class JwtKeyProviderTest {

    private static final long ONE_HOUR_MS = 3_600_000;

    /** Clave de ejemplo de un tutorial muy difundido; la usaba este proyecto. */
    private static final String PUBLIC_TUTORIAL_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Test
    void acceptsARandom256BitSecret() {
        JwtKeyProvider provider = providerFor(randomBase64Secret(32));

        assertThat(provider.getSigningKey()).isNotNull();
    }

    @Test
    void rejectsAMissingSecret() {
        assertThatThrownBy(() -> providerFor(null))
                .isInstanceOf(InsecureJwtSecretException.class)
                .hasMessageContaining("JWT_SECRET_KEY no está definida");

        assertThatThrownBy(() -> providerFor("   "))
                .isInstanceOf(InsecureJwtSecretException.class)
                .hasMessageContaining("JWT_SECRET_KEY no está definida");
    }

    @Test
    void rejectsASecretThatIsNotBase64() {
        assertThatThrownBy(() -> providerFor("esto-no-es-base64!!!"))
                .isInstanceOf(InsecureJwtSecretException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    void rejectsASecretShorterThan256Bits() {
        assertThatThrownBy(() -> providerFor(randomBase64Secret(16)))
                .isInstanceOf(InsecureJwtSecretException.class)
                .hasMessageContaining("256 bits");
    }

    @Test
    void rejectsAPubliclyKnownSecret_EvenThoughItIsLongEnough() {
        assertThatThrownBy(() -> providerFor(PUBLIC_TUTORIAL_SECRET))
                .isInstanceOf(InsecureJwtSecretException.class)
                .hasMessageContaining("clave de ejemplo pública");
    }

    @Test
    void errorMessageNeverRevealsTheSecret() {
        String shortSecret = randomBase64Secret(16);

        assertThatThrownBy(() -> providerFor(shortSecret))
                .hasMessageNotContaining(shortSecret);
        assertThatThrownBy(() -> providerFor(PUBLIC_TUTORIAL_SECRET))
                .hasMessageNotContaining(PUBLIC_TUTORIAL_SECRET);
    }

    private static JwtKeyProvider providerFor(String secret) {
        return new JwtKeyProvider(new JwtProperties(secret, ONE_HOUR_MS));
    }

    private static String randomBase64Secret(int bytes) {
        byte[] secret = new byte[bytes];
        new SecureRandom().nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }
}
