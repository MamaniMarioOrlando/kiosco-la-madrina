package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.exception.InsufficientStockException;
import devMario.example.kioscoLaMadrina.exception.ResourceNotFoundException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

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

        // Reloj fijo: la hora de la venta es predecible y se puede verificar.
        private static final Instant NOW = Instant.parse("2026-10-22T02:30:00Z");

        private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        private SaleServiceImpl saleService;

        private User testUser;
        private Product testProduct;

        @BeforeEach
        void setUp() {
                // Armado a mano, como lo haría Spring: si el servicio suma una dependencia, este test no compila.
                saleService = new SaleServiceImpl(saleRepository, productRepository, userRepository, saleMapper,
                                clock, ZoneId.of("America/Argentina/Buenos_Aires"));

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

                when(userRepository.getRequiredByUsername("testadmin")).thenReturn(testUser);
                when(productRepository.getRequiredById(1L)).thenReturn(testProduct);
                // La base confirma que había stock y lo descontó (1 fila actualizada)
                when(productRepository.decreaseStockIfAvailable(1L, 3)).thenReturn(1);

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
                                devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH, null, null, null);
                when(saleMapper.toDTO(any(Sale.class))).thenReturn(dummyResponse);

                // 2. ACT (Ejecutar el método real)
                SaleResponseDTO response = saleService.createSale(request, "testadmin");

                // 3. ASSERT (Verificar que los resultados computados matemáticamente son
                // correctos)
                assertNotNull(response);

                // El total se verifica en la venta que armó el servicio, no en la respuesta del mapper
                // (que es un mock: verificarla sería comprobar el valor que pusimos nosotros mismos).
                ArgumentCaptor<Sale> savedSale = ArgumentCaptor.forClass(Sale.class);
                verify(saleRepository, times(1)).save(savedSale.capture());
                assertEquals(new BigDecimal("4500"), savedSale.getValue().getTotalAmount()); // 3 x $1500
                assertEquals(NOW, savedSale.getValue().getDateTime());

                // El stock se descuenta en la base con una operación atómica (3 unidades del producto 1)
                verify(productRepository, times(1)).decreaseStockIfAvailable(1L, 3);
        }

        @Test
        void testCreateSale_InsufficientStock_ThrowsException() {
                // 1. ARRANQUE
                // Intentamos comprar 15 unidades, pero solo hay 10 en stock
                SaleItemRequestDTO item = new SaleItemRequestDTO(1L, 15);
                SaleRequestDTO request = new SaleRequestDTO(List.of(item), devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);

                when(userRepository.getRequiredByUsername("testadmin")).thenReturn(testUser);
                when(productRepository.getRequiredById(1L)).thenReturn(testProduct);

                // 2 & 3. ACT & ASSERT
                // Aseguramos que lanzar una venta así, obligatoriamente
                // explote con una InsufficientStockException (la API responde 409)
                InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
                        saleService.createSale(request, "testadmin");
                });

                assertEquals("Stock insuficiente para Coca-Cola: pediste 15, quedan 10",
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
        when(userRepository.getRequiredByUsername("hacker_user"))
                .thenThrow(new ResourceNotFoundException("No se encontró el usuario hacker_user"));

        // 2 & 3. ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            saleService.createSale(request, "hacker_user");
        });

        // Verificamos que no se buscó ningún producto ni se guardó nada
        verify(productRepository, never()).getRequiredById(anyLong());
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void testCreateSale_ProductNotFound_ThrowsException() {
        // 1. ARRANGE
        SaleItemRequestDTO item = new SaleItemRequestDTO(99L, 1); // ID 99 no existe
        SaleRequestDTO request = new SaleRequestDTO(List.of(item), devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH);

        when(userRepository.getRequiredByUsername("testadmin")).thenReturn(testUser);
        when(productRepository.getRequiredById(99L))
                .thenThrow(new ResourceNotFoundException("No se encontró el producto con id 99"));

        // 2 & 3. ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            saleService.createSale(request, "testadmin");
        });
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

        when(userRepository.getRequiredByUsername("testadmin")).thenReturn(testUser);
        when(productRepository.getRequiredById(1L)).thenReturn(testProduct);
        when(productRepository.getRequiredById(2L)).thenReturn(doritos);
        when(productRepository.decreaseStockIfAvailable(1L, 2)).thenReturn(1);
        when(productRepository.decreaseStockIfAvailable(2L, 5)).thenReturn(1);
        
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        SaleResponseDTO dummyResponse = new SaleResponseDTO(1L, null, new BigDecimal("8000"), "testadmin", null, devMario.example.kioscoLaMadrina.model.PaymentMethod.CASH, null, null, null);
        when(saleMapper.toDTO(any(Sale.class))).thenReturn(dummyResponse);

        // 2. ACT
        SaleResponseDTO response = saleService.createSale(request, "testadmin");

        // 3. ASSERT
        assertNotNull(response);

        ArgumentCaptor<Sale> savedSale = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository, times(1)).save(savedSale.capture());
        assertEquals(new BigDecimal("8000"), savedSale.getValue().getTotalAmount(),
                "El total matemático de múltiples productos falló");

        // Verificamos que a AMBOS productos se les haya descontado el stock individualmente
        verify(productRepository, times(1)).decreaseStockIfAvailable(1L, 2);
        verify(productRepository, times(1)).decreaseStockIfAvailable(2L, 5);
    }
}
