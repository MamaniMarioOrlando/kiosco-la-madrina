package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record RegisterRequestDTO(
        @NotBlank @Size(min = 3, max = 20) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 40) String password,
        Set<String> role) {
}
