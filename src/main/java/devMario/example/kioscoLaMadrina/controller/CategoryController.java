package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.CategoryDTO;
import devMario.example.kioscoLaMadrina.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @GetMapping
    public List<CategoryDTO> list() {
        return categoryService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryDTO create(@Valid @RequestBody CategoryDTO dto) {
        return categoryService.create(dto);
    }
}
