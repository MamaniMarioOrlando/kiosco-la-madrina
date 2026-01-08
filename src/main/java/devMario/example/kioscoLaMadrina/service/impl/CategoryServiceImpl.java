package devMario.example.kioscoLaMadrina.service.impl;

import devMario.example.kioscoLaMadrina.dto.CategoryDTO;
import devMario.example.kioscoLaMadrina.model.Category;
import devMario.example.kioscoLaMadrina.repository.CategoryRepository;
import devMario.example.kioscoLaMadrina.service.CategoryService;
import devMario.example.kioscoLaMadrina.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO create(CategoryDTO dto) {
        Category cat = categoryMapper.toEntity(dto);
        return categoryMapper.toDTO(categoryRepository.save(cat));
    }
}
