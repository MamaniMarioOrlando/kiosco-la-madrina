package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.model.Category;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.CategoryRepository;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Decisión de negocio: una categoría con productos no se puede borrar; primero hay que moverlos.
 * Así ningún producto queda "sin categoría" sin que nadie lo note.
 */
@ActiveProfiles("test")
@WithMockUser(username = "admin", authorities = "ADMIN")
class CategoryDeletionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void deletingACategoryWithProducts_Returns409_AndKeepsIt() throws Exception {
        Category drinks = newCategory("Bebidas");
        newProduct(drinks);
        newProduct(drinks);

        mockMvc.perform(delete("/api/categories/{id}", drinks.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "La categoría " + drinks.getName() + " tiene 2 productos; movelos a otra categoría antes de borrarla"));

        assertThat(categoryRepository.existsById(drinks.getId())).isTrue();
    }

    @Test
    void deletingAnEmptyCategory_Succeeds() throws Exception {
        Category empty = newCategory("Vacía");

        mockMvc.perform(delete("/api/categories/{id}", empty.getId()))
                .andExpect(status().isOk());

        assertThat(categoryRepository.existsById(empty.getId())).isFalse();
    }

    @Test
    void deletingAMissingCategory_Returns404() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    private Category newCategory(String name) {
        // Nombre único: la columna es UNIQUE y todos los tests comparten la misma base.
        return categoryRepository.save(Category.builder().name(name + " " + UUID.randomUUID()).build());
    }

    private void newProduct(Category category) {
        productRepository.save(Product.builder()
                .barcode("cat-" + UUID.randomUUID()).name("Producto").price(new BigDecimal("100"))
                .stockQuantity(1).category(category).build());
    }
}
