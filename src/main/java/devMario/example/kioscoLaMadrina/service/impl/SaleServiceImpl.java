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
    public List<SaleResponseDTO> findAll() {
        return saleRepository.findAll().stream()
                .map(saleMapper::toDTO)
                .collect(Collectors.toList());
    }
}
