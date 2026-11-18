package devMario.example.kioscoLaMadrina.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security crea el token CSRF recién cuando alguien lo pide ("diferido"), pensando en páginas que lo
 * meten en un formulario. Acá el frontend lo lee de la cookie XSRF-TOKEN, así que se pide en cada respuesta
 * para que la cookie exista antes del primer pedido que la necesite. Si ya existe, no se escribe de nuevo.
 *
 * No es un @Component a propósito: Spring Boot lo registraría también como filtro suelto, fuera de la
 * cadena de seguridad, donde todavía no hay token.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            token.getToken();
        }
        chain.doFilter(request, response);
    }
}
