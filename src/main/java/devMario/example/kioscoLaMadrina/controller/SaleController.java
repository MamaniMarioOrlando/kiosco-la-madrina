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

    @Operation(summary = "List sales", description = "Retrieves paginated sales, optionally filtered by date.")
    @GetMapping
    public devMario.example.kioscoLaMadrina.dto.PageResponseDTO<SaleResponseDTO> getSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return saleService.getSales(date, page, size);
    }

    @Operation(summary = "Get sales summary", description = "Retrieves the sales summary for a specific date (or all time if no date provided).")
    @GetMapping("/summary")
    public devMario.example.kioscoLaMadrina.dto.DailySalesSummaryDTO getSummary(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return saleService.getSummary(date);
    }

    @Operation(summary = "Get top sellers", description = "Retrieves top 5 selling products")
    @GetMapping("/top-sellers")
    public java.util.List<java.util.Map<String, Object>> getTopSellers() {
        return saleService.getTopSellers();
    }
}
