package devMario.example.kioscoLaMadrina.service.impl;

import devMario.example.kioscoLaMadrina.dto.SaleItemRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.model.*;
import devMario.example.kioscoLaMadrina.repository.*;
import devMario.example.kioscoLaMadrina.service.SaleService;
import devMario.example.kioscoLaMadrina.mapper.SaleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class SaleServiceImpl implements SaleService {

    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SaleMapper saleMapper;

    @Override
    public SaleResponseDTO createSale(SaleRequestDTO request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sale sale = new Sale();
        sale.setUser(user);
        sale.setDateTime(LocalDateTime.now());
        if(request.paymentMethod() != null) {
            sale.setPaymentMethod(request.paymentMethod());
        }

        List<SaleDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequestDTO item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.productId()));

            if (product.getStockQuantity() < item.quantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Update Stock
            product.setStockQuantity(product.getStockQuantity() - item.quantity());
            productRepository.save(product);

            SaleDetail detail = new SaleDetail();
            detail.setSale(sale);
            detail.setProduct(product);
            detail.setQuantity(item.quantity());
            detail.setUnitPrice(product.getPrice());
            detail.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.quantity())));

            details.add(detail);
            totalAmount = totalAmount.add(detail.getSubtotal());
        }

        sale.setDetails(details);
        sale.setTotalAmount(totalAmount);

        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toDTO(savedSale);
    }

    @Override
    @Transactional(readOnly = true)
    public devMario.example.kioscoLaMadrina.dto.PageResponseDTO<SaleResponseDTO> getSales(LocalDate date, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("dateTime").descending());
        org.springframework.data.domain.Page<Sale> salesPage;
        
        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
            salesPage = saleRepository.findByDateTimeBetween(startOfDay, endOfDay, pageable);
        } else {
            salesPage = saleRepository.findAll(pageable);
        }

        List<SaleResponseDTO> content = salesPage.getContent().stream()
                .map(saleMapper::toDTO)
                .collect(Collectors.toList());

        return new devMario.example.kioscoLaMadrina.dto.PageResponseDTO<>(
                content,
                salesPage.getNumber(),
                salesPage.getSize(),
                salesPage.getTotalElements(),
                salesPage.getTotalPages(),
                salesPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public devMario.example.kioscoLaMadrina.dto.DailySalesSummaryDTO getSummary(LocalDate date) {
        LocalDateTime startOfDay = date != null ? date.atStartOfDay() : LocalDate.of(1970, 1, 1).atStartOfDay();
        LocalDateTime endOfDay = date != null ? date.atTime(23, 59, 59, 999999999) : LocalDateTime.now().plusYears(100);

        List<Object[]> results = saleRepository.getSalesSummaryByPaymentMethodAndDate(startOfDay, endOfDay);
        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal mpTotal = BigDecimal.ZERO;

        for (Object[] row : results) {
            PaymentMethod method = (PaymentMethod) row[0];
            BigDecimal sum = (row[1] != null) ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            if (method == PaymentMethod.MERCADO_PAGO) {
                mpTotal = mpTotal.add(sum);
            } else {
                cashTotal = cashTotal.add(sum); // Defaults to cash
            }
        }

        return new devMario.example.kioscoLaMadrina.dto.DailySalesSummaryDTO(
                cashTotal,
                mpTotal,
                cashTotal.add(mpTotal)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getTopSellers() {
        org.springframework.data.domain.Pageable topFive = org.springframework.data.domain.PageRequest.of(0, 5);
        List<Object[]> results = saleRepository.getTopSellersQuery(topFive);
        return results.stream().map(row -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", row[0]);
            map.put("quantity", row[1]);
            return map;
        }).collect(Collectors.toList());
    }
}
