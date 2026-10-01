package devMario.example.kioscoLaMadrina.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Diagnóstico de IPs detrás de proxies: muestra qué dirección ve la aplicación y qué informan los encabezados.
 * Apagado por defecto; se enciende en producción con LOGGING_LEVEL_KIOSCO_CLIENTIP=DEBUG.
 */
class ClientIpLoggingFilterTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ClientIpLoggingFilter.LOGGER_NAME);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final ClientIpLoggingFilter filter = new ClientIpLoggingFilter();

    @BeforeEach
    void captureLogs() {
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void releaseLogs() {
        logger.detachAppender(logs);
        logger.setLevel(null);
    }

    @Test
    void loggerNameIsLowercase_SoAnEnvironmentVariableCanEnableIt() {
        // Spring Boot pasa a minúsculas los nombres de logger que vienen de variables de entorno.
        assertThat(ClientIpLoggingFilter.LOGGER_NAME).isEqualTo(ClientIpLoggingFilter.LOGGER_NAME.toLowerCase());
    }

    @Test
    void whenEnabled_LogsTheResolvedAddressAndTheProxyHeaders() throws Exception {
        logger.setLevel(Level.DEBUG);

        filter.doFilter(apiRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(logs.list).hasSize(1);
        assertThat(logs.list.get(0).getFormattedMessage())
                .contains("fd12:3456::1", "203.0.113.5", "198.51.100.9", "/api/auth/signin");
    }

    @Test
    void isSilentByDefault() throws Exception {
        logger.setLevel(Level.INFO);

        filter.doFilter(apiRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(logs.list).isEmpty();
    }

    private static MockHttpServletRequest apiRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/signin");
        request.setRemoteAddr("fd12:3456::1");
        request.addHeader("X-Forwarded-For", "203.0.113.5");
        request.addHeader("X-Real-IP", "198.51.100.9");
        return request;
    }
}
