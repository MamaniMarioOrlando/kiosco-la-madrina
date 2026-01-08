package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);
}
