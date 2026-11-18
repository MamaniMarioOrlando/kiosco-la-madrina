package devMario.example.kioscoLaMadrina.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.time.Duration;
import java.util.Optional;

/**
 * La cookie que lleva el JWT de la sesión del navegador. Un único lugar sabe cómo se arma, así el login y
 * el logout no pueden quedar con atributos distintos (si el Path no coincide, el navegador no la borra).
 *
 * - HttpOnly: JavaScript no puede leerla. Un XSS ya no puede robar la sesión.
 * - Secure: solo viaja por HTTPS (los navegadores hacen una excepción con localhost para desarrollo).
 * - SameSite=Strict: el navegador no la manda en pedidos que nacen en otro sitio. Primera línea contra CSRF.
 * - Path=/api: solo acompaña a los pedidos a la API, no a cada página o imagen.
 */
@Component
public class SessionCookie {

    public static final String NAME = "kiosco_session";
    private static final String PATH = "/api";

    private final Duration maxAge;

    public SessionCookie(JwtProperties properties) {
        // Igual que el token: una cookie que durara más que el JWT solo traería un token vencido.
        this.maxAge = properties.jwtExpiration();
    }

    public ResponseCookie create(String jwt) {
        return base(jwt).maxAge(maxAge).build();
    }

    /** Max-Age=0 con los mismos atributos: el navegador la borra. */
    public ResponseCookie clear() {
        return base("").maxAge(Duration.ZERO).build();
    }

    /** Estático a propósito: el filtro del JWT lo usa y así no depende de la configuración del JWT. */
    public static Optional<String> read(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, NAME))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank());
    }

    private static ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(PATH);
    }
}
