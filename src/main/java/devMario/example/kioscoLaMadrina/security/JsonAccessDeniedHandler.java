package devMario.example.kioscoLaMadrina.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Usuario autenticado pero sin permiso (403). Escribe la respuesta directamente en vez de
 * sendError(), que dispara un forward a /error y pierde el formato de la API.
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponder responder;

    public JsonAccessDeniedHandler(SecurityErrorResponder responder) {
        this.responder = responder;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        responder.write(request, response, HttpStatus.FORBIDDEN,
                "No tienes permisos suficientes para realizar esta acción.");
    }
}
