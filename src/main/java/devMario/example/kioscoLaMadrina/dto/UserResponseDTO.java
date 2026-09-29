package devMario.example.kioscoLaMadrina.dto;

import devMario.example.kioscoLaMadrina.model.Role;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        Role role,
        boolean active) {
}
