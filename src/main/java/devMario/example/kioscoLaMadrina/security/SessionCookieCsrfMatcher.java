package devMario.example.kioscoLaMadrina.security;

import devMario.example.kioscoLaMadrina.security.jwt.SessionCookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Set;

/**
 * Decide qué pedidos tienen que traer el token CSRF: los que cambian datos y se autentican con la cookie.
 *
 * El ataque CSRF existe porque el navegador agrega la cookie solo, aunque el pedido lo haya armado otra
 * página. Por eso:
 * - GET/HEAD/OPTIONS/TRACE no cambian nada: no hace falta.
 * - Con "Authorization" no hay cookie en juego: ese encabezado nunca lo agrega el navegador por su cuenta.
 *   (AuthTokenFilter, si viene el encabezado, ignora la cookie: no se puede combinar uno con la otra.)
 * - Sin cookie de sesión no hay nada que robarle a nadie: el pedido sigue y recibe su 401.
 * - El login no usa la sesión: autentica con usuario y contraseña, y además solo acepta JSON, que un
 *   formulario de otro sitio no puede mandar.
 */
public class SessionCookieCsrfMatcher implements RequestMatcher {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final String SIGNIN_PATH = "/api/auth/signin";

    @Override
    public boolean matches(HttpServletRequest request) {
        return !SAFE_METHODS.contains(request.getMethod())
                && request.getHeader(HttpHeaders.AUTHORIZATION) == null
                && SessionCookie.read(request).isPresent()
                && !SIGNIN_PATH.equals(request.getServletPath());
    }
}
