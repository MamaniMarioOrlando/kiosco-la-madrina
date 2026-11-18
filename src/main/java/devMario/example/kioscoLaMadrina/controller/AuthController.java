package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.AuthRequestDTO;
import devMario.example.kioscoLaMadrina.dto.JwtResponseDTO;
import devMario.example.kioscoLaMadrina.security.jwt.SessionCookie;
import devMario.example.kioscoLaMadrina.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Login y logout. El alta de usuarios es responsabilidad de un ADMIN (ver {@link UserController}).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User login and logout")
public class AuthController {

    private final AuthService authService;
    private final SessionCookie sessionCookie;

    public AuthController(AuthService authService, SessionCookie sessionCookie) {
        this.authService = authService;
        this.sessionCookie = sessionCookie;
    }

    @Operation(summary = "Sign in user",
            description = "Authenticates a user. The browser session travels in an httpOnly cookie; "
                    + "the token in the body is for API clients (Authorization: Bearer).")
    @PostMapping("/signin")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest,
                                                           HttpServletRequest request) {
        JwtResponseDTO session = authService.login(loginRequest, request.getRemoteAddr());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.create(session.token()).toString())
                .body(session);
    }

    /**
     * Borra la cookie del navegador. El JWT sigue siendo válido hasta que vence (no hay lista de tokens
     * revocados): quien lo hubiera copiado antes podría seguir usándolo. Por eso dura horas, no semanas.
     */
    @Operation(summary = "Sign out", description = "Clears the browser session cookie.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.clear().toString())
                .build();
    }
}
