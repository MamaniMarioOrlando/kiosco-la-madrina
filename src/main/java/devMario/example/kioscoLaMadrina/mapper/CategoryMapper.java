package devMario.example.kioscoLaMadrina.mapper;

import devMario.example.kioscoLaMadrina.dto.CategoryDTO;
import devMario.example.kioscoLaMadrina.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category category);

    Category toEntity(CategoryDTO dto);
}
