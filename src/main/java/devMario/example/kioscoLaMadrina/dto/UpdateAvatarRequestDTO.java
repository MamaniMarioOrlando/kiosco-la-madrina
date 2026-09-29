package devMario.example.kioscoLaMadrina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAvatarRequestDTO(
        @NotBlank
        // El frontend comprime a 200x200 JPEG (~20 KB); 200.000 caracteres deja margen sin permitir abusos.
        @Size(max = 200_000, message = "la imagen es demasiado grande")
        // Solo formatos raster: SVG puede contener scripts y una URL externa permitiría rastrear a quien la vea.
        @Pattern(regexp = "^data:image/(png|jpeg|webp);base64,[A-Za-z0-9+/]+={0,2}$",
                message = "debe ser una imagen PNG, JPEG o WEBP en formato data URL")
        String avatarUrl) {
}
