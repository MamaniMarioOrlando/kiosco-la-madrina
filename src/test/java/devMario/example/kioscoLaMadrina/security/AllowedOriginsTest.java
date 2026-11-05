package devMario.example.kioscoLaMadrina.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ALLOWED_ORIGINS se lee una sola vez al arrancar. Un valor peligroso o vacío tiene que
 * frenar el arranque (fail-fast) en vez de dejar la API abierta o rota en silencio.
 */
class AllowedOriginsTest {

    @Test
    void parse_SingleOrigin_ReturnsIt() {
        assertThat(AllowedOrigins.parse("http://localhost:3001"))
                .containsExactly("http://localhost:3001");
    }

    @Test
    void parse_CommaSeparatedWithSpaces_TrimsEachOrigin() {
        // Sin trim, " https://b.com" (con espacio) nunca coincidiría con el header Origin.
        assertThat(AllowedOrigins.parse("https://a.com, https://b.com ,"))
                .containsExactly("https://a.com", "https://b.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"*", "https://a.com,*", "https://*.vercel.app"})
    void parse_Wildcard_FailsFast(String raw) {
        assertThatThrownBy(() -> AllowedOrigins.parse(raw))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ALLOWED_ORIGINS")
                .hasMessageContaining("*");
    }

    @Test
    void parse_OriginWithPort_IsAccepted() {
        assertThat(AllowedOrigins.parse("http://localhost:3001,https://kiosco.example:8443"))
                .containsExactly("http://localhost:3001", "https://kiosco.example:8443");
    }

    // El navegador manda el header Origin como esquema://host[:puerto], sin barra ni ruta:
    // cualquier otra forma nunca coincidiría y CORS rechazaría al frontend sin explicar por qué.
    @ParameterizedTest
    @ValueSource(strings = {
            "https://kiosco-la-madrina.vercel.app/",
            "https://kiosco-la-madrina.vercel.app/login",
            "kiosco-la-madrina.vercel.app",
            "ftp://kiosco.example",
            "https://kiosco.example?x=1"
    })
    void parse_MalformedOrigin_FailsFast(String raw) {
        assertThatThrownBy(() -> AllowedOrigins.parse(raw))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ALLOWED_ORIGINS")
                .hasMessageContaining(raw);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", " , ,"})
    void parse_Empty_FailsFast(String raw) {
        assertThatThrownBy(() -> AllowedOrigins.parse(raw))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ALLOWED_ORIGINS");
    }
}
