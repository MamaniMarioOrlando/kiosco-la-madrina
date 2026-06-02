package devMario.example.kioscoLaMadrina.dto;

import java.util.List;

public record PageResponseDTO<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {
}
