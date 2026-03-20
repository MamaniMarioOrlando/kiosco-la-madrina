package devMario.example.kioscoLaMadrina.dto;

import java.util.List;

public record JwtResponseDTO(
        String token,
        String type,
        Long id,
        String username,
        String email,
        String avatarUrl,
        List<String> roles) {
    public JwtResponseDTO(String accessToken, Long id, String username, String email, String avatarUrl, List<String> roles) {
        this(accessToken, "Bearer", id, username, email, avatarUrl, roles);
    }
}
