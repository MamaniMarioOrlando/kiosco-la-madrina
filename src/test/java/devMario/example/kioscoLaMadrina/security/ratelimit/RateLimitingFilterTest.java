package devMario.example.kioscoLaMadrina.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.security.RateLimitingFilter;
import devMario.example.kioscoLaMadrina.security.SecurityErrorResponder;
import io.github.bucket4j.Bandwidth;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** Límite general de la API por IP. Test unitario: el filtro recibe un registro con un límite chico. */
class RateLimitingFilterTest {

    private final RateLimitingFilter filter = new RateLimitingFilter(
            new BucketRegistry(Bandwidth.builder().capacity(2).refillGreedy(2, Duration.ofMinutes(1)).build(), 1_000),
            new SecurityErrorResponder(new ObjectMapper().registerModule(new JavaTimeModule())));

    @Test
    void blocksAnIpThatExceedsTheLimit_WithoutAffectingOthers() throws Exception {
        assertThat(call("/api/products", "1.1.1.1")).isEqualTo(200);
        assertThat(call("/api/products", "1.1.1.1")).isEqualTo(200);
        assertThat(call("/api/products", "1.1.1.1")).isEqualTo(429);

        assertThat(call("/api/products", "2.2.2.2")).isEqualTo(200);
    }

    @Test
    void blockedRequest_GetsTheApiJsonErrorFormat_AndRetryAfter() throws Exception {
        call("/api/products", "3.3.3.3");
        call("/api/products", "3.3.3.3");

        MockHttpServletResponse blocked = request("/api/products", "3.3.3.3");

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getContentType()).startsWith("application/json");
        assertThat(JsonPath.<String>read(blocked.getContentAsString(), "$.message"))
                .contains("Demasiadas peticiones");
        // Con 2 fichas por minuto, la próxima ficha llega en 30 s como máximo.
        assertThat(Integer.parseInt(blocked.getHeader("Retry-After"))).isBetween(1, 30);
    }

    @Test
    void onlyLimitsApiRoutes() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(call("/swagger-ui/index.html", "1.1.1.1")).isEqualTo(200);
        }
    }

    private int call(String uri, String ip) throws Exception {
        return request(uri, ip).getStatus();
    }

    private MockHttpServletResponse request(String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
