package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.exception.ResourceNotFoundException;
import devMario.example.kioscoLaMadrina.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Descuenta stock en una sola operación atómica: la base verifica y descuenta a la vez, así dos ventas
     * simultáneas nunca leen el mismo stock (lost update). Devuelve 1 si se descontó y 0 si no alcanzaba.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity "
            + "WHERE p.id = :id AND p.stockQuantity >= :quantity")
    int decreaseStockIfAvailable(@Param("id") Long id, @Param("quantity") int quantity);

    /** Devuelve mercadería al stock (por ejemplo, al anular una venta). Atómico, igual que el descuento. */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    void increaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    /** Busca un producto que debe existir; si no existe, la API responde 404. */
    default Product getRequiredById(Long id) {
        return findById(id).orElseThrow(
                () -> new ResourceNotFoundException("No se encontró el producto con id " + id));
    }
    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    /** Productos que hay que reponer: activos y con menos unidades que el umbral, los más urgentes primero. */
    List<Product> findByActiveTrueAndStockQuantityLessThanOrderByStockQuantityAscIdAsc(int threshold);

    /** Productos (activos o no) que referencian la categoría: todos impiden borrarla. */
    long countByCategoryId(Long categoryId);

    /** Catálogo: solo los productos que se pueden vender. */
    List<Product> findByActiveTrue();

    List<Product> findByActiveFalse();
}
