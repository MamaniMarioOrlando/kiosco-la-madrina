package devMario.example.kioscoLaMadrina.security;

import devMario.example.kioscoLaMadrina.security.jwt.AuthEntryPointJwt;
import devMario.example.kioscoLaMadrina.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter; // Making Filter managed by Spring properly
    private final RateLimitingFilter rateLimitingFilter;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    // Inyección de dependencias por constructor
    public WebSecurityConfig(AuthEntryPointJwt unauthorizedHandler,
            AuthTokenFilter authTokenFilter,
            RateLimitingFilter rateLimitingFilter,
            JsonAccessDeniedHandler accessDeniedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.authTokenFilter = authTokenFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    // Sin AuthenticationProvider propio: Spring Security arma el DaoAuthenticationProvider con el único
    // UserDetailsService (UserDetailsServiceImpl) y el PasswordEncoder de abajo. Declararlo a mano repetía eso.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                // Double submit: el backend deja el token en la cookie XSRF-TOKEN (legible por JavaScript) y
                // el frontend lo copia en el encabezado X-XSRF-TOKEN. Otra página no puede leer esa cookie,
                // así que no puede armar el encabezado. Solo se exige con la cookie de sesión.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        // Token tal cual, sin el enmascarado contra BREACH: ese ataque lee el token de una
                        // respuesta HTML comprimida, y acá nunca viaja en el cuerpo, solo en la cookie.
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(new SessionCookieCsrfMatcher()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // El contenedor hace un forward a /error tras un sendError(); si /error exigiera token,
                        // un 403 (sin permisos) le llegaría al cliente como 401 (sesión inválida).
                        .requestMatchers("/error").permitAll()
                        // Mínimo privilegio: solo el login es público (ruta y método exactos). Todo lo demás
                        // cae en anyRequest().authenticated(), así un endpoint nuevo nunca queda abierto sin querer.
                        .requestMatchers(HttpMethod.POST, "/api/auth/signin").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Defensa en profundidad: además del @PreAuthorize, se corta en el filtro,
                        // antes de que la validación del body revele detalles a quien no tiene permiso.
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated());

        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        return http.build();
    }

    private static CookieCsrfTokenRepository csrfTokenRepository() {
        // HttpOnly=false a propósito: el frontend TIENE que poder leerla. Lo que la protege es que otra
        // página no puede leer cookies de este dominio (Same-Origin Policy).
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.sameSite("Strict"));
        return repository;
    }

    @Value("${ALLOWED_ORIGINS:http://localhost:3001}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(AllowedOrigins.parse(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));

        // Seguro porque AllowedOrigins rechaza el comodín: solo dominios exactos pueden mandar credenciales.
        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
