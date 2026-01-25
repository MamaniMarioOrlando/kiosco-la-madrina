package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.ProductDTO;
import devMario.example.kioscoLaMadrina.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Management of products inventory")
public class ProductController {
    @Autowired
    ProductService productService;

    @Operation(summary = "List products", description = "Retrieves all products.")
    @GetMapping
    public List<ProductDTO> list() {
        return productService.findAll();
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID.")
    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @Operation(summary = "Create product", description = "Creates a new product. Requires ADMIN role.")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductDTO create(@Valid @RequestBody ProductDTO dto) {
        return productService.create(dto);
    }

    @Operation(summary = "Update product", description = "Updates an existing product. Requires ADMIN role.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductDTO update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        return productService.update(id, dto);
    }

    @Operation(summary = "Delete product", description = "Deletes a product by ID. Requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
