package devMario.example.kioscoLaMadrina.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Guarda las IPs temporalmente en memoria conectadas a su balde de 30 tokens
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // Asignamos 300 Peticiones que se rellenan cada 1 minuto
        Bandwidth limit = Bandwidth.builder()
            .capacity(300)
            .refillGreedy(300, Duration.ofMinutes(1))
            .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Solo limitamos las rutas de nuestra propia API, omitimos archivos de UI Swagger
        if (request.getRequestURI().startsWith("/api/")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Demasiadas peticiones. Has sido bloqueado temporalmente por seguridad.");
                return; // Corta la barrera y nunca llega a la base de datos
            }
        }

        // Si gastó una ficha con exito, sigue el curso normal
        filterChain.doFilter(request, response);
    }
}
