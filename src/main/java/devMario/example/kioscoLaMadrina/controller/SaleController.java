package devMario.example.kioscoLaMadrina.controller;

import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.service.SaleService;
import jakarta.validation.Valid;
import devMario.example.kioscoLaMadrina.dto.VoidSaleRequestDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "Management of sales transactions")
public class SaleController {
    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @Operation(summary = "Register sale", description = "Creates a new sale transaction for the authenticated user.")
    @PostMapping
    public SaleResponseDTO createSale(@Valid @RequestBody SaleRequestDTO request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return saleService.createSale(request, userDetails.getUsername());
    }

    @Operation(summary = "Void sale", description = "Voids a sale from today and returns its items to stock. Admin only.")
    @PostMapping("/{id}/void")
    @PreAuthorize("hasAuthority('ADMIN')")
    public SaleResponseDTO voidSale(@PathVariable Long id, @Valid @RequestBody VoidSaleRequestDTO request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return saleService.voidSale(id, request.reason(), userDetails.getUsername());
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
    public java.util.List<devMario.example.kioscoLaMadrina.dto.TopSellerDTO> getTopSellers() {
        return saleService.getTopSellers();
    }
}
