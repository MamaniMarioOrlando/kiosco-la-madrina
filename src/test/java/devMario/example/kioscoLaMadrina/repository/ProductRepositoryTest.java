package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.model.Category;
import devMario.example.kioscoLaMadrina.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ¡LA MAGIA DE @DataJpaTest!
// Esta anotación apaga la Web, apaga los Controllers, apaga los Servicios y apaga tu PostgreSQL real.
// Enciende SOLAMENTE H2 (base en memoria súper rápida) y configura Hibernate. ¡Todo automáticamente!
@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Tu "Inyector" oficial para bases de datos de prueba

    @Autowired
    private ProductRepository productRepository; // Lo que estamos testeando hoy

    @Test
    void testFindByStockQuantityLessThan_ReturnsProductsInRisk() {
        // 1. ARRANGE (PREPARACIÓN EL ESCENARIO)
        // Guardamos una categoría real en memoria
        Category category = new Category();
        category.setName("Snacks de Prueba");
        category = entityManager.persist(category);

        // Creamos nuestro almacén ficticio usando Setters (Para evitar problemas de constructores)
        Product product1 = new Product();
        product1.setBarcode("779001");
        product1.setName("Papas Lays");
        product1.setPrice(new BigDecimal("1000"));
        product1.setStockQuantity(50); // Mucho stock (A salvo)
        product1.setCategory(category);
        
        Product product2 = new Product();
        product2.setBarcode("779002");
        product2.setName("Gomitas");
        product2.setPrice(new BigDecimal("200"));
        product2.setStockQuantity(3);  // POCO STOCK (¡Peligro!)
        product2.setCategory(category);

        Product product3 = new Product();
        product3.setBarcode("779003");
        product3.setName("Bidu Cola");
        product3.setPrice(new BigDecimal("800"));
        product3.setStockQuantity(8);  // POCO STOCK (¡Peligro!)
        product3.setCategory(category);

        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);
        entityManager.flush(); // Forzamos escribirlos en la BD en memoria (H2)

        // 2. ACT (EJECUCIÓN DEL MÉTODO REBELDE)
        // El Project Manager definió que un umbral menor a 10 dispara una alerta a reposición.
        List<Product> endangeredProducts = productRepository.findByStockQuantityLessThan(10);

        // 3. ASSERT (COMPROBACIÓN DEL EFECTO)
        // Spring Data JPA tuvo que convertir "findByStockQuantityLessThan" a un SELECT mágico de SQL.
        // Si lo hizo bien, Papas Lays NO debe estar en la lista. Solo deben haber 2 (Gomitas y Bidu).
        assertEquals(2, endangeredProducts.size(), "Debería encontrar exactamente 2 productos a punto de agotarse");
        
        boolean foundGomitas = endangeredProducts.stream().anyMatch(p -> p.getName().equals("Gomitas"));
        boolean foundBiduCola = endangeredProducts.stream().anyMatch(p -> p.getName().equals("Bidu Cola"));
        
        assertTrue(foundGomitas, "Gomitas debería estar en la lista de alerta");
        assertTrue(foundBiduCola, "Bidu Cola debería estar en la lista de alerta");
    }
}
