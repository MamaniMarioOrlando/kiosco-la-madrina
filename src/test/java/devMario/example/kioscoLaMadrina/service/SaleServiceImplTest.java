package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.dto.SaleItemRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.model.Product;
import devMario.example.kioscoLaMadrina.model.Sale;
import devMario.example.kioscoLaMadrina.model.User;
import devMario.example.kioscoLaMadrina.repository.ProductRepository;
import devMario.example.kioscoLaMadrina.repository.SaleRepository;
import devMario.example.kioscoLaMadrina.repository.UserRepository;
import devMario.example.kioscoLaMadrina.service.impl.SaleServiceImpl;
import devMario.example.kioscoLaMadrina.mapper.SaleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleServiceImplTest {

        @Mock
        private SaleRepository saleRepository;

        @Mock
        private ProductRepository productRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SaleMapper saleMapper;

        @InjectMocks
        private SaleServiceImpl saleService;

        private User testUser;
        private Product testProduct;

        @BeforeEach
        void setUp() {
                // Configuramos datos falsos iniciales antes de cada test
                testUser = new User();
                testUser.setUsername("testadmin");

                testProduct = new Product();
                testProduct.setId(1L);
                testProduct.setName("Coca-Cola");
                testProduct.setPrice(new BigDecimal("1500"));
                testProduct.setStockQuantity(10);
        }

        @Test
        void testCreateSale_Success() {
                // 1. ARRANQUE (Preparar el escenario y programar los Mocks)
                SaleItemRequestDTO item = new SaleItemRequestDTO(1L, 3); // Intentamos comprar 3 unidades
                SaleRequestDTO request = new SaleRequestDTO(List.of(item), devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);

                when(userRepository
                                .findByUsername("testadmin"))
                                .thenReturn(Optional.of(testUser));
                when(productRepository.findById(1L))
                                .thenReturn(Optional.of(testProduct));

                // Cuando guarde la venta, devolveremos la misma venta
                when(saleRepository
                                .save(any(Sale.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Solo necesitamos que el mapper devuelva algo para no dar NullPointerException
                SaleResponseDTO dummyResponse = new SaleResponseDTO(
                                1L,
                                null,
                                new BigDecimal("4500"),
                                "testadmin",
                                null,
                                devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);
                when(saleMapper.toDTO(any(Sale.class))).thenReturn(dummyResponse);

                // 2. ACT (Ejecutar el método real)
                SaleResponseDTO response = saleService.createSale(request, "testadmin");

                // 3. ASSERT (Verificar que los resultados computados matemáticamente son
                // correctos)
                assertNotNull(response);
                assertEquals(new BigDecimal("4500"), response.totalAmount());

                // Verificar que el stock se haya restado (10 - 3 = 7)
                assertEquals(7, testProduct.getStockQuantity());

                // Verificar que los repositorios fueron llamados exactamente una vez
                verify(productRepository, times(1)).save(testProduct);
                verify(saleRepository, times(1)).save(any(Sale.class));
        }

        @Test
        void testCreateSale_InsufficientStock_ThrowsException() {
                // 1. ARRANQUE
                // Intentamos comprar 15 unidades, pero solo hay 10 en stock
                SaleItemRequestDTO item = new SaleItemRequestDTO(1L, 15);
                SaleRequestDTO request = new SaleRequestDTO(List.of(item), devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);

                when(userRepository
                                .findByUsername("testadmin"))
                                .thenReturn(Optional.of(testUser));
                when(productRepository
                                .findById(1L))
                                .thenReturn(Optional.of(testProduct));

                // 2 & 3. ACT & ASSERT
                // Aseguramos que lanzar una venta así, obligatoriamente
                // explote con una RuntimeException
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        saleService.createSale(request, "testadmin");
                });

                assertEquals("Insufficient stock for product: Coca-Cola",
                                exception.getMessage());

                // Verificar que el stock NO SE TOCÓ
                assertEquals(10, testProduct.getStockQuantity());

                // Verificar que ABSOLUTAMENTE NADA se guardó en la Base de Datos
                verify(saleRepository, never()).save(any(Sale.class));
        }

    @Test
    void testCreateSale_UserNotFound_ThrowsException() {
        // 1. ARRANGE
        SaleItemRequestDTO item = new SaleItemRequestDTO(1L, 1);
        SaleRequestDTO request = new SaleRequestDTO(List.of(item), devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);

        // Simulamos que el usuario fue borrado o no existe
        when(userRepository.findByUsername("hacker_user")).thenReturn(Optional.empty());

        // 2 & 3. ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            saleService.createSale(request, "hacker_user");
        });

        assertEquals("User not found", exception.getMessage());
        
        // Verificamos que no se buscó ningún producto ni se guardó nada
        verify(productRepository, never()).findById(anyLong());
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void testCreateSale_ProductNotFound_ThrowsException() {
        // 1. ARRANGE
        SaleItemRequestDTO item = new SaleItemRequestDTO(99L, 1); // ID 99 no existe
        SaleRequestDTO request = new SaleRequestDTO(List.of(item), devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);

        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // 2 & 3. ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            saleService.createSale(request, "testadmin");
        });

        assertEquals("Product not found: 99", exception.getMessage());
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void testCreateSale_MultipleItems_Success() {
        // 1. ARRANGE
        // Creamos un segundo producto para este test
        Product doritos = new Product();
        doritos.setId(2L);
        doritos.setName("Doritos");
        doritos.setPrice(new BigDecimal("1000"));
        doritos.setStockQuantity(20);

        // Cliente pide 2 Coca-Colas ($1500 c/u = $3000) y 5 Doritos ($1000 c/u = $5000). TOTAL esperado: $8000
        SaleRequestDTO request = new SaleRequestDTO(List.of(
                new SaleItemRequestDTO(1L, 2),
                new SaleItemRequestDTO(2L, 5)
        ), devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);

        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findById(2L)).thenReturn(Optional.of(doritos));
        
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        SaleResponseDTO dummyResponse = new SaleResponseDTO(1L, null, new BigDecimal("8000"), "testadmin", null, devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);
        when(saleMapper.toDTO(any(Sale.class))).thenReturn(dummyResponse);

        // 2. ACT
        SaleResponseDTO response = saleService.createSale(request, "testadmin");

        // 3. ASSERT
        assertNotNull(response);
        assertEquals(new BigDecimal("8000"), response.totalAmount(), "El total matemático de múltiples productos falló");
        
        // Verificamos que a AMBOS productos se les haya restado el stock individualmente
        assertEquals(8, testProduct.getStockQuantity()); // 10 - 2 = 8
        assertEquals(15, doritos.getStockQuantity());    // 20 - 5 = 15

        // Verificamos que el repositorio intentó guardar cambios 2 veces (una por cada producto)
        verify(productRepository, times(2)).save(any(Product.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }
}
