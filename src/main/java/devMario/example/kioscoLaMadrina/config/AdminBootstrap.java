package devMario.example.kioscoLaMadrina.config;

import devMario.example.kioscoLaMadrina.dto.CreateUserRequestDTO;
import devMario.example.kioscoLaMadrina.model.Role;
import devMario.example.kioscoLaMadrina.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Crea el primer ADMIN al arrancar si todavía no existe ninguno.
 * Sin registro público, es la única forma de entrar a un sistema recién instalado.
 * Es idempotente: si ya hay un admin, no hace nada.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminBootstrapProperties properties;
    private final UserService userService;
    private final Validator validator;

    public AdminBootstrap(AdminBootstrapProperties properties, UserService userService, Validator validator) {
        this.properties = properties;
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userService.adminExists()) {
            return;
        }

        if (!properties.isConfigured()) {
            logger.warn("No existe ningún ADMIN y no se definieron ADMIN_USERNAME/ADMIN_PASSWORD. "
                    + "Nadie podrá administrar el sistema hasta configurarlas y reiniciar.");
            return;
        }

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                properties.username(), properties.email(), properties.password(), Role.ADMIN);

        // Fail fast: mejor no arrancar que crear un admin con una contraseña débil.
        Set<ConstraintViolation<CreateUserRequestDTO>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("Configuración del admin inicial inválida: " + errors);
        }

        userService.create(request);
        logger.info("Se creó el administrador inicial '{}'", properties.username());
    }
}
