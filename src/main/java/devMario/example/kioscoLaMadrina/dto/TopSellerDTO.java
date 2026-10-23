package devMario.example.kioscoLaMadrina.dto;

/** Un producto del ranking de más vendidos. Se identifica por id: dos productos pueden llamarse igual. */
public record TopSellerDTO(
        Long productId,
        String name,
        Long quantity) {
}
