package devMario.example.kioscoLaMadrina.security.jwt;

import devMario.example.kioscoLaMadrina.security.SecurityErrorResponder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Petición sin token válido (401): el frontend lo interpreta como "sesión vencida" y manda al login.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    private final SecurityErrorResponder responder;

    public AuthEntryPointJwt(SecurityErrorResponder responder) {
        this.responder = responder;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        logger.debug("Unauthorized request to {}: {}", request.getRequestURI(), authException.getMessage());
        responder.write(request, response, HttpStatus.UNAUTHORIZED,
                "Tu sesión no es válida o expiró. Iniciá sesión nuevamente.");
    }
}
