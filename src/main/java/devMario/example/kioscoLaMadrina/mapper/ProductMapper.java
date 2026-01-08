package devMario.example.kioscoLaMadrina.mapper;

import devMario.example.kioscoLaMadrina.dto.ProductDTO;
import devMario.example.kioscoLaMadrina.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductDTO toDTO(Product product);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "active", constant = "true")
    Product toEntity(ProductDTO dto);
}
