package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.exception.ResourceNotFoundException;
import devMario.example.kioscoLaMadrina.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Busca un producto que debe existir; si no existe, la API responde 404. */
    default Product getRequiredById(Long id) {
        return findById(id).orElseThrow(
                () -> new ResourceNotFoundException("No se encontró el producto con id " + id));
    }
    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    // NUEVO MÉTODO (Búsqueda Rara): Nos sirve para encontrar productos a punto de agotarse
    List<Product> findByStockQuantityLessThan(Integer threshold);
}
