package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.CategoryDTO;
import devMario.example.kioscoLaMadrina.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Management of product categories")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @Operation(summary = "List categories", description = "Retrieves all available product categories.")
    @GetMapping
    public List<CategoryDTO> list() {
        return categoryService.findAll();
    }

    @Operation(summary = "Create category", description = "Creates a new category. Requires ADMIN role.")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryDTO create(@Valid @RequestBody CategoryDTO dto) {
        return categoryService.create(dto);
    }

    @Operation(summary = "Update category", description = "Updates an existing category. Requires ADMIN role.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryDTO update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return categoryService.update(id, dto);
    }

    @Operation(summary = "Delete category", description = "Deletes a category. Requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
