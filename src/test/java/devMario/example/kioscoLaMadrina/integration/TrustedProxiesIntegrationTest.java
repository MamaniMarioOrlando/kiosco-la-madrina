package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.test.context.ActiveProfiles;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proxies de los que se acepta X-Forwarded-For con la configuración por defecto (segura).
 * Solo redes privadas: un cliente de internet no puede hacerse pasar por proxy para falsificar su IP.
 * Por eso en Railway, cuyos proxies tienen IPs públicas, hay que habilitarlo con
 * SERVER_TOMCAT_REMOTEIP_INTERNALPROXIES (ver application.yaml).
 */
@ActiveProfiles("test")
class TrustedProxiesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ServerProperties serverProperties;

    private Pattern trustedProxies;

    @BeforeEach
    void loadDefaultPattern() {
        trustedProxies = Pattern.compile(serverProperties.getTomcat().getRemoteip().getInternalProxies());
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.0.0.5", "192.168.1.10", "172.16.0.1", "100.64.0.1", "127.0.0.1", "::1"})
    void privateAddressesAreTrustedByDefault(String address) {
        assertThat(trustedProxies.matcher(address).matches()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "152.233.23.193", "152.233.23.194",   // proxies de Railway observados en producción
            "203.0.113.5", "8.8.8.8", "2001:db8::1"
    })
    void publicAddressesAreNotTrustedByDefault(String address) {
        assertThat(trustedProxies.matcher(address).matches()).isFalse();
    }
}
