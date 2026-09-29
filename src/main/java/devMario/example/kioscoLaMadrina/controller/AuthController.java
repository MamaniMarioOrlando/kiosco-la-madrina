package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.AuthRequestDTO;
import devMario.example.kioscoLaMadrina.dto.JwtResponseDTO;
import devMario.example.kioscoLaMadrina.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Solo expone el login. El alta de usuarios es responsabilidad de un ADMIN (ver {@link UserController}).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Sign in user", description = "Authenticates a user and returns a JWT token.")
    @PostMapping("/signin")
    public JwtResponseDTO authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest) {
        return authService.login(loginRequest);
    }
}
