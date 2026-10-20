package devMario.example.kioscoLaMadrina.repository;

import devMario.example.kioscoLaMadrina.exception.ResourceNotFoundException;
import devMario.example.kioscoLaMadrina.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Busca una categoría que debe existir; si no existe, la API responde 404. */
    default Category getRequiredById(Long id) {
        return findById(id).orElseThrow(
                () -> new ResourceNotFoundException("No se encontró la categoría con id " + id));
    }
}
