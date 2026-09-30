package devMario.example.kioscoLaMadrina.dto;

import devMario.example.kioscoLaMadrina.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequestDTO(
        @NotBlank
        @Size(min = 3, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "solo puede contener letras, números, '.', '_' y '-'")
        String username,

        @NotBlank @Email String email,

        // BCrypt ignora todo lo que supere los 72 bytes: aceptar más daría una falsa sensación de seguridad.
        @NotBlank @Size(min = 8, max = 72) String password,

        @NotNull Role role) {
}
