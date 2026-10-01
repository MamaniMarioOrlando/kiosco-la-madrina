package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Qué direcciones trata Tomcat como proxy de confianza al leer X-Forwarded-For.
 * Railway conecta su proxy al contenedor por su red interna IPv6, que el valor por defecto
 * de Spring Boot no incluye: por eso la app veía la IP del proxy en lugar de la del cliente.
 * Solo pueden ser de confianza direcciones privadas, que nunca llegan desde internet.
 */
@ActiveProfiles("test")
class TrustedProxiesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private Environment environment;

    private Pattern trustedProxies;

    @BeforeEach
    void loadConfiguredPattern() {
        trustedProxies = Pattern.compile(environment.getRequiredProperty("server.tomcat.remoteip.internal-proxies"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "fd12:3456:789a::1",      // red privada IPv6 (Railway)
            "fc00::1",
            "10.0.0.5", "192.168.1.10", "172.16.0.1", "100.64.0.1", "127.0.0.1", "::1"
    })
    void privateAddressesAreTrusted(String address) {
        assertThat(trustedProxies.matcher(address).matches()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2001:db8::1",            // IPv6 pública: un cliente, nunca un proxy de confianza
            "2800:810:4a5::1",
            "203.0.113.5", "181.45.10.20", "8.8.8.8",
            "fe00::1"                 // no es parte de fc00::/7
    })
    void publicAddressesAreNotTrusted(String address) {
        assertThat(trustedProxies.matcher(address).matches()).isFalse();
    }
}
