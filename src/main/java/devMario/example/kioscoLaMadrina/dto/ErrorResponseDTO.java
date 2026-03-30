package devMario.example.kioscoLaMadrina.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
