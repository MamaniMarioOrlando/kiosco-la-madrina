package devMario.example.kioscoLaMadrina.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Credenciales del primer administrador, leídas de variables de entorno
 * (ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL).
 */
@ConfigurationProperties(prefix = "kiosco.bootstrap.admin")
public record AdminBootstrapProperties(String username, String password, String email) {

    public boolean isConfigured() {
        return StringUtils.hasText(username) && StringUtils.hasText(password);
    }
}
