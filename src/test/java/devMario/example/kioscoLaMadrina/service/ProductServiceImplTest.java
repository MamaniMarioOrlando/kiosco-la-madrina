package devMario.example.kioscoLaMadrina.service;

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
        // La lógica en ProductServiceImpl dice: if (!existsById(id))
        // throw error.
        // Falsificamos para que MIENTA y diga que sí existe.
        when(productRepository.existsById(1L)).thenReturn(true);

        // NO podemos usar when(repo.deleteById).thenReturn(true)...
        // ¡porque deleteById no devuelve nada (void)!
        // En Mockito, por defecto, los métodos void de un @Mock
        // literalmenten "no hacen nada", lo cual es perfecto.
        // Podríamos forzarlo con:
        // doNothing().when(productRepository).deleteById(1L);
        // (pero no es obligatorio)

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
        // ...auditamos al propio repositorio para confirmar que Java
        // le dio la orden final de matar al registro.
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProduct_NotFound_ThrowsException() {
        // 1. ARRANGE
        // Simulamos que el Kiosquero intentó borrar
        // el producto fantasma "99"
        when(productRepository.existsById(99L)).thenReturn(false);

        // 2 & 3. ACT & ASSERT
        // Esto captura la excepción justo antes de que rompa
        // el servidor
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.delete(99L);
        });

        assertEquals("Product not found", exception.getMessage());

        // Verificaciones Policiales:
        verify(productRepository, times(1)).existsById(99L);
        // Sí buscaron el ID 99
        verify(productRepository, never()).deleteById(anyLong());
        // Pero jamás dispararon la orden de borrar en la BD
    }
}
