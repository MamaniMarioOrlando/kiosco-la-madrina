package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.dto.PageResponseDTO;
import devMario.example.kioscoLaMadrina.dto.DailySalesSummaryDTO;

import java.time.LocalDate;

public interface SaleService {
    SaleResponseDTO createSale(SaleRequestDTO request, String username);

    PageResponseDTO<SaleResponseDTO> getSales(LocalDate date, int page, int size);
    
    DailySalesSummaryDTO getSummary(LocalDate date);

    java.util.List<java.util.Map<String, Object>> getTopSellers();
}
