package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.dto.ProductDTO;
import java.util.List;

public interface ProductService {
    /** Catálogo: solo productos activos. */
    List<ProductDTO> findAll();

    List<ProductDTO> findInactive();

    /** Productos activos que hay que reponer, los más urgentes primero. */
    List<ProductDTO> findLowStock();

    ProductDTO updateStatus(Long id, boolean active);

    ProductDTO findById(Long id);

    ProductDTO create(ProductDTO productDTO);

    ProductDTO update(Long id, ProductDTO productDTO);

    void delete(Long id);
}
