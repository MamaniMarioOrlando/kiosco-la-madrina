package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
