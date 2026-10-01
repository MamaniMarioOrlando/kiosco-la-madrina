package devMario.example.kioscoLaMadrina.security;

import devMario.example.kioscoLaMadrina.security.ratelimit.BucketRegistry;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Límite general de pedidos a la API por IP (algoritmo token bucket). */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final BucketRegistry apiRateLimits;
    private final SecurityErrorResponder errorResponder;

    public RateLimitingFilter(@Qualifier("apiRateLimits") BucketRegistry apiRateLimits,
                              SecurityErrorResponder errorResponder) {
        this.apiRateLimits = apiRateLimits;
        this.errorResponder = errorResponder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Solo se limitan las rutas de la API, no los archivos de la UI de Swagger.
        if (request.getRequestURI().startsWith("/api/")) {
            ConsumptionProbe probe = apiRateLimits.bucketFor(request.getRemoteAddr()).tryConsumeAndReturnRemaining(1);
            if (!probe.isConsumed()) {
                // Retry-After (estándar HTTP): en cuántos segundos llega la próxima ficha.
                long seconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(seconds));
                errorResponder.write(request, response, HttpStatus.TOO_MANY_REQUESTS,
                        "Demasiadas peticiones. Esperá unos segundos y volvé a intentar.");
                return; // Corta antes de llegar al resto de la aplicación y a la base de datos.
            }
        }

        filterChain.doFilter(request, response);
    }
}
