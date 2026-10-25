package devMario.example.kioscoLaMadrina.service.impl;

import devMario.example.kioscoLaMadrina.dto.ProductDTO;
import devMario.example.kioscoLaMadrina.exception.DuplicateBarcodeException;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.CategoryRepository;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import devMario.example.kioscoLaMadrina.service.ProductService;
import devMario.example.kioscoLaMadrina.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAll() {
        return productRepository.findByActiveTrue().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findInactive() {
        return productRepository.findByActiveFalse().stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    public ProductDTO updateStatus(Long id, boolean active) {
        Product product = productRepository.getRequiredById(id);
        if (active) {
            product.activate();
        } else {
            product.deactivate();
        }
        return productMapper.toDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        return productMapper.toDTO(productRepository.getRequiredById(id));
    }

    @Override
    public ProductDTO create(ProductDTO productDTO) {
        ensureBarcodeIsFree(productDTO.barcode(), null);
        Product product = productMapper.toEntity(productDTO);

        if (productDTO.categoryId() != null) {
            product.setCategory(categoryRepository.getRequiredById(productDTO.categoryId()));
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Override
    public ProductDTO update(Long id, ProductDTO productDTO) {
        Product product = productRepository.getRequiredById(id);
        ensureBarcodeIsFree(productDTO.barcode(), id);

        product.setName(productDTO.name());
        product.setPrice(productDTO.price());
        product.setStockQuantity(productDTO.stockQuantity());
        product.setBarcode(productDTO.barcode());

        if (productDTO.categoryId() != null) {
            product.setCategory(categoryRepository.getRequiredById(productDTO.categoryId()));
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDTO(updatedProduct);
    }

    @Override
    public void delete(Long id) {
        // Borrado lógico: el producto deja de venderse pero sigue existiendo para el historial de ventas
        // (borrarlo de verdad lo impedía la base, o dejaba ventas sin saber qué producto se vendió).
        productRepository.getRequiredById(id).deactivate();
    }

    /**
     * El código de barras identifica un producto: no puede repetirse (tampoco con uno inactivo,
     * que lo conserva). ownerId es el producto que se está editando, que sí puede mantener su código.
     */
    private void ensureBarcodeIsFree(String barcode, Long ownerId) {
        productRepository.findByBarcode(barcode)
                .filter(existing -> !existing.getId().equals(ownerId))
                .ifPresent(existing -> {
                    throw new DuplicateBarcodeException(existing);
                });
    }
}
