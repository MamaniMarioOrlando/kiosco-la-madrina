package devMario.example.kioscoLaMadrina.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Usuario autenticado pero sin permiso (403), o pedido sin un token CSRF válido. Escribe la respuesta directamente en vez de
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
        // Sin token CSRF el problema no es de permisos: lo normal es que la cookie XSRF-TOKEN se haya
        // perdido o cambiado (otra pestaña, cookies borradas). Recargar la página trae una nueva.
        String message = accessDeniedException instanceof CsrfException
                ? "La sesión de seguridad venció. Recargá la página e intentá de nuevo."
                : "No tienes permisos suficientes para realizar esta acción.";
        responder.write(request, response, HttpStatus.FORBIDDEN, message);
    }
}
