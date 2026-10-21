package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.exception.ResourceNotFoundException;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import devMario.example.kioscoLaMadrina.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testDeleteProduct_Success_TestingVoid() {
        // 1. ARRANGE
        // ProductServiceImpl busca el producto con getRequiredById (404 si no existe).
        // Falsificamos para que MIENTA y devuelva un producto existente.
        Product existing = new Product();
        when(productRepository.getRequiredById(1L)).thenReturn(existing);

        // 2. ACT
        // El método NO devuelve variables. Así que usamos
        // assertDoesNotThrow para afirmar que "se ejecuta
        // en silencio sin explotar".
        assertDoesNotThrow(() -> {
            productService.delete(1L);
        });

        // 3. ASSERT / VERIFY
        // ¡El corazón de la prueba void! Ya que no tenemos un "resultado"
        // para analizar con assertEquals...
        // ...verificamos el efecto: borrado LÓGICO. El producto queda inactivo
        // y jamás se elimina de la base (lo necesita el historial de ventas).
        verify(productRepository, times(1)).getRequiredById(1L);
        assertFalse(existing.isActive());
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void testDeleteProduct_NotFound_ThrowsException() {
        // 1. ARRANGE
        // Simulamos que el Kiosquero intentó borrar
        // el producto fantasma "99"
        when(productRepository.getRequiredById(99L))
                .thenThrow(new ResourceNotFoundException("No se encontró el producto con id 99"));

        // 2 & 3. ACT & ASSERT
        // Esto captura la excepción justo antes de que llegue al cliente
        // (el GlobalExceptionHandler la convierte en un 404)
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.delete(99L);
        });

        // Verificaciones Policiales:
        verify(productRepository, times(1)).getRequiredById(99L);
        // Sí buscaron el ID 99
        verify(productRepository, never()).delete(any(Product.class));
        // Pero jamás dispararon la orden de borrar en la BD
    }
}
