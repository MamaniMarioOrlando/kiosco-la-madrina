package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "Management of sales transactions")
public class SaleController {
    @Autowired
    SaleService saleService;

    @Operation(summary = "Register sale", description = "Creates a new sale transaction for the authenticated user.")
    @PostMapping
    public SaleResponseDTO createSale(@Valid @RequestBody SaleRequestDTO request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return saleService.createSale(request, userDetails.getUsername());
    }

    @Operation(summary = "List sales", description = "Retrieves all recorded sales.")
    @GetMapping
    public List<SaleResponseDTO> list() {
        return saleService.findAll();
    }
}
