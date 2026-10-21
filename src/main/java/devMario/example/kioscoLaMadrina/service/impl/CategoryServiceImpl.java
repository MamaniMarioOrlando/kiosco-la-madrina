package devMario.example.kioscoLaMadrina.service.impl;

import devMario.example.kioscoLaMadrina.dto.CategoryDTO;
import devMario.example.kioscoLaMadrina.exception.ConflictException;
import devMario.example.kioscoLaMadrina.model.Category;
import devMario.example.kioscoLaMadrina.repository.CategoryRepository;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import devMario.example.kioscoLaMadrina.service.CategoryService;
import devMario.example.kioscoLaMadrina.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository,
                               CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO create(CategoryDTO dto) {
        Category cat = categoryMapper.toEntity(dto);
        return categoryMapper.toDTO(categoryRepository.save(cat));
    }

    @Override
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.getRequiredById(id);
        category.setName(dto.name());
        category.setDescription(dto.description());
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.getRequiredById(id);

        // Decisión de negocio: no se borra una categoría con productos (ni siquiera inactivos),
        // así ningún producto queda "sin categoría" sin que nadie lo note.
        long products = productRepository.countByCategoryId(id);
        if (products > 0) {
            throw new ConflictException("La categoría %s tiene %d %s; movelos a otra categoría antes de borrarla"
                    .formatted(category.getName(), products, products == 1 ? "producto" : "productos"));
        }

        categoryRepository.delete(category);
    }
}
