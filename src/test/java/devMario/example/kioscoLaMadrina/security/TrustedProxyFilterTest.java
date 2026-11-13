package devMario.example.kioscoLaMadrina.security;

import jakarta.servlet.ServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El proxy de Next.js (Vercel) informa la IP real del cliente. Solo se le cree si trae el secreto compartido:
 * cualquiera puede llamar a Railway directo y mandar el encabezado de la IP inventado.
 */
class TrustedProxyFilterTest {

    private static final String SECRET = "s3cr3t-compartido-entre-vercel-y-railway";
    private static final String CONNECTION_IP = "76.76.21.21"; // quien se conecta: Vercel, o un atacante

    private final TrustedProxyFilter filter = new TrustedProxyFilter(SECRET);

    @Test
    void withTheSecret_TheClientIpSentByTheProxyIsUsed() throws Exception {
        assertThat(remoteAddrSeenByTheApp(filter, SECRET, "181.45.12.7")).isEqualTo("181.45.12.7");
    }

    @Test
    void acceptsIpv6ClientAddresses() throws Exception {
        assertThat(remoteAddrSeenByTheApp(filter, SECRET, "2800:810:4a3::1")).isEqualTo("2800:810:4a3::1");
    }

    @Test
    void withAWrongSecret_TheHeaderIsIgnored() throws Exception {
        assertThat(remoteAddrSeenByTheApp(filter, "adivinando", "1.2.3.4")).isEqualTo(CONNECTION_IP);
    }

    @Test
    void withoutTheSecret_TheHeaderIsIgnored() throws Exception {
        assertThat(remoteAddrSeenByTheApp(filter, null, "1.2.3.4")).isEqualTo(CONNECTION_IP);
    }

    @Test
    void anInvalidClientIp_IsIgnoredEvenWithTheSecret() throws Exception {
        // Basura o una lista: nunca debe convertirse en una clave del rate limiting.
        assertThat(remoteAddrSeenByTheApp(filter, SECRET, "no-soy-una-ip")).isEqualTo(CONNECTION_IP);
        assertThat(remoteAddrSeenByTheApp(filter, SECRET, "1.2.3.4, 5.6.7.8")).isEqualTo(CONNECTION_IP);
        assertThat(remoteAddrSeenByTheApp(filter, SECRET, "kiosco.example.com")).isEqualTo(CONNECTION_IP);
        assertThat(remoteAddrSeenByTheApp(filter, SECRET, "999.1.1.1")).isEqualTo(CONNECTION_IP);
        assertThat(remoteAddrSeenByTheApp(filter, SECRET, "cafe")).isEqualTo(CONNECTION_IP); // hex, pero no IPv6
    }

    @Test
    void withoutAConfiguredSecret_NoProxyIsTrusted() throws Exception {
        // Desarrollo local sin proxy: un secreto vacío jamás coincide, ni siquiera con un encabezado vacío.
        TrustedProxyFilter disabled = new TrustedProxyFilter("");
        assertThat(remoteAddrSeenByTheApp(disabled, "", "1.2.3.4")).isEqualTo(CONNECTION_IP);
        assertThat(remoteAddrSeenByTheApp(new TrustedProxyFilter(null), null, "1.2.3.4"))
                .isEqualTo(CONNECTION_IP);
    }

    @Test
    void rejectsASecretTooShortToResistGuessing() {
        assertThatThrownBy(() -> new TrustedProxyFilter("corto"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PROXY_SECRET");
    }

    private static String remoteAddrSeenByTheApp(TrustedProxyFilter filter, String secret, String clientIp)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/signin");
        request.setRemoteAddr(CONNECTION_IP);
        if (secret != null) {
            request.addHeader(TrustedProxyFilter.SECRET_HEADER, secret);
        }
        request.addHeader(TrustedProxyFilter.CLIENT_IP_HEADER, clientIp);

        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        ServletRequest seen = chain.getRequest();
        return seen.getRemoteAddr();
    }
}
