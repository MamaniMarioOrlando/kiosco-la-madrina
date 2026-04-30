package devMario.example.kioscoLaMadrina.integration;

import devMario.example.kioscoLaMadrina.config.AbstractIntegrationTest;
import devMario.example.kioscoLaMadrina.model.Category;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.CategoryRepository;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// MAGIA PURA: Al extender de AbstractIntegrationTest, Spring Boot sabe que
// debe ir a buscar el contenedor de Docker para PostgreSQL antes de correr la prueba.
public class ProductIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // El navegador simulado

    @Autowired
    private ProductRepository productRepository; // El acceso directo a PostgreSQL

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @WithMockUser(authorities = "ADMIN") // Porque solo el Admin puede crear productos
    void testCreateProduct_IntegratesWithPostgreSQL_Successfully() throws Exception {

        // 1. ARRANGE (PREPARACIÓN)
        // Guardar una categoría real en la Base de Datos Docker
        Category cat = new Category();
        cat.setName("Golosinas");
        cat.setDescription("Dulces varios");
        Category savedCat = categoryRepository.save(cat);

        // Diseñamos el JSON que enviaría React
        String productJson = """
                {
                    "barcode": "999888",
                    "name": "Alfajor Triple",
                    "price": 1500.50,
                    "stockQuantity": 100,
                    "categoryId": %d
                }
                """.formatted(savedCat.getId());

        // 2. ACT (ACCIÓN)
        // Disparamos la petición Web
        mockMvc.perform(post("/api/products")
                .with(csrf()) // Evadimos la seguridad anti-hackers para el POST
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isCreated()); // Aserción Web: Respuesta 201 Created

        // 3. ASSERT (COMPROBACIÓN FINAL EN LA BASE DE DATOS)
        // ¿Realmente se guardó de forma física en el disco duro del Docker?
        Optional<Product> foundProduct = productRepository.findByBarcode("999888");

        // Si Testcontainers fallara o el Controlador no guardara el dato, esto
        // devolvería FALSE.
        assertTrue(foundProduct.isPresent(), "El producto debería estar insertado en PostgreSQL");
        assertEquals("Alfajor Triple", foundProduct.get().getName(), "El nombre insertado debe coincidir");
        assertEquals(100, foundProduct.get().getStockQuantity(), "El stock físico en Postgres debe ser 100");
    }
}
