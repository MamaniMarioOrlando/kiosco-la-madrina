package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.ProductDTO;
import devMario.example.kioscoLaMadrina.dto.UpdateStatusRequestDTO;
import devMario.example.kioscoLaMadrina.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Management of products inventory")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "List products", description = "Retrieves the catalog: active products only.")
    @GetMapping
    public List<ProductDTO> list() {
        return productService.findAll();
    }

    @Operation(summary = "List inactive products", description = "Deactivated products, which can be reactivated. Requires ADMIN role.")
    @GetMapping("/inactive")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<ProductDTO> listInactive() {
        return productService.findInactive();
    }

    @Operation(summary = "Activate or deactivate product", description = "Inactive products are not sold nor listed. Requires ADMIN role.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductDTO updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequestDTO request) {
        return productService.updateStatus(id, request.active());
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID.")
    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @Operation(summary = "Create product", description = "Creates a new product. Requires ADMIN role.")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ProductDTO create(@Valid @RequestBody ProductDTO dto) {
        return productService.create(dto);
    }

    @Operation(summary = "Update product", description = "Updates an existing product. Requires ADMIN role.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductDTO update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        return productService.update(id, dto);
    }

    @Operation(summary = "Delete product", description = "Deactivates the product (soft delete); it stays in the sales history. Requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
