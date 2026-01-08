package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.dto.CategoryDTO;
import java.util.List;

public interface CategoryService {
    List<CategoryDTO> findAll();

    CategoryDTO create(CategoryDTO dto);
}
