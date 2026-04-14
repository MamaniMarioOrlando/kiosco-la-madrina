package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.ProductDTO;
import devMario.example.kioscoLaMadrina.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class })
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc; // Tu "Navegador de Internet" integrado en Java

    @MockitoBean
    private ProductService productService; // Falsificamos el Servicio (ya lo probamos ayer)

    @MockitoBean
    private devMario.example.kioscoLaMadrina.security.jwt.JwtUtils jwtUtils;

    @MockitoBean
    private devMario.example.kioscoLaMadrina.security.services.UserDetailsServiceImpl userDetailsService;

    // PATRÓN PROFESIONAL: ObjectMapper convierte DTOs a JSON
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Test
    void testGetAllProducts_ReturnsListAndHttp200() throws Exception {
        // 1. ARRANGE
        ProductDTO fakeProduct = new ProductDTO(1L, "12345", "Coca-Cola",
                new BigDecimal("1500"), 10, 1L, "Bebidas");
        when(productService.findAll()).thenReturn(Arrays.asList(fakeProduct));

        // 2 & 3. ACT & ASSERT
        // Ordenamos al navegador hacer una petición HTTP GET de verdad,
        // ¡y analizamos los paquetes de internet!
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Validamos que Java responda con un maravilloso HTTP 200 (OK)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Validamos que el Header indique JSON y
                                                                              // no un HTML por error
                .andExpect(jsonPath("$[0].name").value("Coca-Cola")) // Comprobamos que el texto que se le manda a React
                                                                     // esté correcto
                .andExpect(jsonPath("$[0].price").value(1500))
                .andExpect(jsonPath("$[0].stockQuantity").value(10));
    }

    @Test
    void testCreateProduct_InvalidData_ReturnsBadRequest() throws Exception {
        // 1. ARRANGE
        // Creamos un DTO corrupto (Precio negativo, y stock numéricamente imposible)
        // Esto rompe las reglas: @Positive para precio y @Min(0) para stock.
        ProductDTO corruptProduct = new ProductDTO(
                null, 
                "77912345", 
                "Alfajor Roto", 
                new BigDecimal("-500"), // PRECIO INVIABLE (Genera Error)
                -5, // STOCK INVIABLE (Genera Error)
                1L, 
                "Golosinas"
        );

        // Convertimos el objeto Java a un String JSON usando nuestra herramienta ObjectMapper
        String jsonCorrupto = objectMapper.writeValueAsString(corruptProduct);

        // 2 & 3. ACT & ASSERT
        // Simulamos una petición POST hacia tu API enviando la basura JSON
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCorrupto))
                
                // ASSERTION MAGISTRAL:
                // No revisamos el contenido interno de las variables.
                // Exigimos que el Escudo de Spring Boot (@Valid) haya interceptado el ataque ANTES de llegar al Servicio.
                // Si el escudo funcionó, tu método de Controller jamás se ejecutó y Spring respondió con HTTP 400 (Bad Request).
                .andExpect(status().isBadRequest());
    }
}
