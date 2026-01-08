package devMario.example.kioscoLaMadrina.service;

import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import java.util.List;

public interface SaleService {
    SaleResponseDTO createSale(SaleRequestDTO request, String username);

    List<SaleResponseDTO> findAll();
}
