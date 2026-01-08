package devMario.example.kioscoLaMadrina.dto;

import java.util.List;

public record JwtResponseDTO(
        String token,
        String type,
        Long id,
        String username,
        String email,
        List<String> roles) {
    public JwtResponseDTO(String accessToken, Long id, String username, String email, List<String> roles) {
        this(accessToken, "Bearer", id, username, email, roles);
    }
}
