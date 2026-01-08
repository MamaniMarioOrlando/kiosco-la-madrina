package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
