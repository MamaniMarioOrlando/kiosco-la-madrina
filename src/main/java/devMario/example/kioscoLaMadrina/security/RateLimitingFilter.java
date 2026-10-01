package devMario.example.kioscoLaMadrina.security;

import devMario.example.kioscoLaMadrina.security.ratelimit.BucketRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Límite general de pedidos a la API por IP (algoritmo token bucket). */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final BucketRegistry apiRateLimits;

    public RateLimitingFilter(@Qualifier("apiRateLimits") BucketRegistry apiRateLimits) {
        this.apiRateLimits = apiRateLimits;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Solo se limitan las rutas de la API, no los archivos de la UI de Swagger.
        if (request.getRequestURI().startsWith("/api/")
                && !apiRateLimits.bucketFor(request.getRemoteAddr()).tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Demasiadas peticiones. Has sido bloqueado temporalmente por seguridad.");
            return; // Corta antes de llegar al resto de la aplicación y a la base de datos.
        }

        filterChain.doFilter(request, response);
    }
}
