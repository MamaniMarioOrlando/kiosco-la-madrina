package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.security.jwt.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sin JWT_EXPIRATION configurada, una sesión dura 12 horas: un turno de trabajo con margen.
 * Así el empleado se loguea una vez por turno y una sesión olvidada se cierra sola.
 */
@ActiveProfiles("test")
class JwtConfigurationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void sessionsLastTwelveHoursByDefault() {
        assertThat(jwtProperties.jwtExpiration()).isEqualTo(Duration.ofHours(12));
    }
}
