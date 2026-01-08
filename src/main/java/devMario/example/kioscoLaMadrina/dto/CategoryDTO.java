package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryDTO(Long id, @NotBlank String name, String description) {
}
