package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sales")
public class SaleController {
    @Autowired
    SaleService saleService;

    @PostMapping
    public SaleResponseDTO createSale(@Valid @RequestBody SaleRequestDTO request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return saleService.createSale(request, userDetails.getUsername());
    }

    @GetMapping
    public List<SaleResponseDTO> list() {
        return saleService.findAll();
    }
}
