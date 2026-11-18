package devMario.example.kioscoLaMadrina.integration;

import com.jayway.jsonpath.JsonPath;
import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.security.jwt.SessionCookie;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sesión del navegador en una cookie httpOnly: JavaScript no puede leerla, así que un XSS ya no puede
 * robar el token. Como el navegador la manda solo, los pedidos que cambian datos exigen además el token
 * CSRF (cookie XSRF-TOKEN copiada en el encabezado X-XSRF-TOKEN, algo que otro sitio no puede hacer).
 *
 * Los clientes que mandan "Authorization: Bearer" (Swagger, scripts, tests) siguen igual: el navegador
 * nunca agrega ese encabezado por su cuenta, así que no hay CSRF posible.
 */
@ActiveProfiles("test")
class CookieSessionIntegrationTest extends AbstractIntegrationTest {

    private static final String XSRF_COOKIE = "XSRF-TOKEN";
    private static final String XSRF_HEADER = "X-XSRF-TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void signin_SetsAnHttpOnlySecureStrictSessionCookie() throws Exception {
        String setCookie = signin().getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(header -> header.startsWith(SessionCookie.NAME + "="))
                .findFirst().orElseThrow();

        assertThat(setCookie)
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=Strict")
                .contains("Path=/api")
                .contains("Max-Age=43200"); // 12 h, lo mismo que dura el JWT
    }

    @Test
    void theSessionCookieAloneAuthenticatesReads() throws Exception {
        mockMvc.perform(get("/api/categories").cookie(sessionCookie(signin())))
                .andExpect(status().isOk());
    }

    @Test
    void writesWithTheCookieButWithoutTheCsrfToken_AreRejected() throws Exception {
        // Lo que lograría una página maliciosa: el navegador agrega la cookie, pero no puede leer XSRF-TOKEN.
        mockMvc.perform(post("/api/categories")
                        .cookie(sessionCookie(signin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategory()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        "La sesión de seguridad venció. Recargá la página e intentá de nuevo."));
    }

    @Test
    void writesWithTheCookieAndTheCsrfToken_Succeed() throws Exception {
        MockHttpServletResponse login = signin();
        String csrf = cookieValue(login, XSRF_COOKIE);

        mockMvc.perform(post("/api/categories")
                        .cookie(sessionCookie(login), new Cookie(XSRF_COOKIE, csrf))
                        .header(XSRF_HEADER, csrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategory()))
                .andExpect(status().isOk());
    }

    @Test
    void theCsrfTokenSurvivesAuthenticatedRequests() throws Exception {
        // Como navega una persona: login, varias pantallas (GET) y recién después una venta (POST).
        // Spring rota el token CSRF en cada "inicio de sesión", y con JWT cada pedido autentica de nuevo:
        // sin cuidarlo, cada GET borraba XSRF-TOKEN y el siguiente POST recibía 403.
        MockHttpServletResponse login = signin();
        Cookie session = sessionCookie(login);
        Cookie csrfCookie = new Cookie(XSRF_COOKIE, cookieValue(login, XSRF_COOKIE));

        MockHttpServletResponse read = mockMvc.perform(get("/api/categories").cookie(session, csrfCookie))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertThat(read.getHeaders(HttpHeaders.SET_COOKIE))
                .as("un GET autenticado no toca la cookie XSRF-TOKEN")
                .noneMatch(header -> header.startsWith(XSRF_COOKIE + "="));

        mockMvc.perform(post("/api/categories")
                        .cookie(session, csrfCookie)
                        .header(XSRF_HEADER, csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategory()))
                .andExpect(status().isOk());
    }

    @Test
    void aCsrfTokenThatDoesNotMatchItsCookie_IsRejected() throws Exception {
        MockHttpServletResponse login = signin();

        mockMvc.perform(post("/api/categories")
                        .cookie(sessionCookie(login), new Cookie(XSRF_COOKIE, cookieValue(login, XSRF_COOKIE)))
                        .header(XSRF_HEADER, "inventado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategory()))
                .andExpect(status().isForbidden());
    }

    @Test
    void bearerClients_DoNotNeedTheCsrfToken() throws Exception {
        String token = JsonPath.read(signin().getContentAsString(), "$.token");

        mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategory()))
                .andExpect(status().isOk());
    }

    @Test
    void anAuthorizationHeader_NeverFallsBackToTheCookie() throws Exception {
        // Si no fuera así, "Authorization: Bearer basura" + la cookie esquivaría el control CSRF.
        mockMvc.perform(post("/api/categories")
                        .cookie(sessionCookie(signin()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer basura")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategory()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestsWithoutAnyCredentials_StillGet401() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategory()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_ClearsTheSessionCookie() throws Exception {
        MockHttpServletResponse login = signin();
        String csrf = cookieValue(login, XSRF_COOKIE);

        MockHttpServletResponse logout = mockMvc.perform(post("/api/auth/logout")
                        .cookie(sessionCookie(login), new Cookie(XSRF_COOKIE, csrf))
                        .header(XSRF_HEADER, csrf))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        assertThat(logout.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(header -> assertThat(header)
                        .startsWith(SessionCookie.NAME + "=;")
                        .contains("Max-Age=0")
                        .contains("Path=/api"));
    }

    @Test
    void logout_IsAlsoProtectedAgainstCsrf() throws Exception {
        // Si no, cualquier página podría cerrarle la sesión al cajero en plena venta.
        mockMvc.perform(post("/api/auth/logout").cookie(sessionCookie(signin())))
                .andExpect(status().isForbidden());
    }

    private MockHttpServletResponse signin() throws Exception {
        return mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"AdminPass123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    private static Cookie sessionCookie(MockHttpServletResponse response) {
        return new Cookie(SessionCookie.NAME, cookieValue(response, SessionCookie.NAME));
    }

    private static String cookieValue(MockHttpServletResponse response, String name) {
        Cookie cookie = response.getCookie(name);
        assertThat(cookie).as("la respuesta trae la cookie %s", name).isNotNull();
        return cookie.getValue();
    }

    private static String newCategory() {
        return "{\"name\":\"Cookie " + UUID.randomUUID().toString().substring(0, 8) + "\"}";
    }
}
