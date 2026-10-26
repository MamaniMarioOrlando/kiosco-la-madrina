package devMario.example.kioscoLaMadrina.service.impl;

import devMario.example.kioscoLaMadrina.dto.SaleItemRequestDTO;
import devMario.example.kioscoLaMadrina.dto.DailySalesSummaryDTO;
import devMario.example.kioscoLaMadrina.dto.PageResponseDTO;
import devMario.example.kioscoLaMadrina.dto.PaymentMethodTotalDTO;
import devMario.example.kioscoLaMadrina.dto.TopSellerDTO;
import devMario.example.kioscoLaMadrina.exception.InsufficientStockException;
import devMario.example.kioscoLaMadrina.exception.ProductNotAvailableException;
import devMario.example.kioscoLaMadrina.exception.SaleAlreadyVoidedException;
import devMario.example.kioscoLaMadrina.exception.SaleNotVoidableException;
import devMario.example.kioscoLaMadrina.dto.SaleRequestDTO;
import devMario.example.kioscoLaMadrina.dto.SaleResponseDTO;
import devMario.example.kioscoLaMadrina.model.*;
import devMario.example.kioscoLaMadrina.repository.*;
import devMario.example.kioscoLaMadrina.service.SaleService;
import devMario.example.kioscoLaMadrina.mapper.SaleMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Collectors;
import java.util.List;

@Service
@Transactional
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SaleMapper saleMapper;
    private final Clock clock;
    private final ZoneId businessZone;

    public SaleServiceImpl(SaleRepository saleRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           SaleMapper saleMapper,
                           Clock clock,
                           ZoneId businessZone) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.saleMapper = saleMapper;
        this.clock = clock;
        this.businessZone = businessZone;
    }

    @Override
    public SaleResponseDTO createSale(SaleRequestDTO request, String username) {
        User user = userRepository.getRequiredByUsername(username);

        Sale sale = new Sale();
        sale.setUser(user);
        sale.setDateTime(Instant.now(clock));
        if(request.paymentMethod() != null) {
            sale.setPaymentMethod(request.paymentMethod());
        }

        for (SaleItemRequestDTO item : request.items()) {
            Product product = productRepository.getRequiredById(item.productId());
            if (!product.isActive()) {
                throw new ProductNotAvailableException(product.getName());
            }

            // Verificar y descontar en un solo paso atómico: leer el stock acá y restarlo después
            // permitía que varias cajas vendieran la misma unidad. Si falla, la transacción entera
            // se revierte (incluidos los ítems ya descontados de esta venta).
            if (productRepository.decreaseStockIfAvailable(product.getId(), item.quantity()) == 0) {
                throw new InsufficientStockException(product.getName(), item.quantity(), product.getStockQuantity());
            }

            sale.addItem(product, item.quantity());
        }

        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toDTO(savedSale);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SaleResponseDTO> getSales(LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateTime").descending());
        Page<Sale> salesPage;
        
        if (date != null) {
            salesPage = saleRepository.findByDateTimeGreaterThanEqualAndDateTimeLessThan(
                    startOf(date), startOf(date.plusDays(1)), pageable);
        } else {
            salesPage = saleRepository.findAll(pageable);
        }

        List<SaleResponseDTO> content = salesPage.getContent().stream()
                .map(saleMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
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
    public DailySalesSummaryDTO getSummary(LocalDate date) {
        List<PaymentMethodTotalDTO> totals = date != null
                ? saleRepository.getSalesSummaryByPaymentMethodBetween(startOf(date), startOf(date.plusDays(1)))
                : saleRepository.getSalesSummaryByPaymentMethod();
        BigDecimal cashTotal = BigDecimal.ZERO;
        BigDecimal mpTotal = BigDecimal.ZERO;

        for (PaymentMethodTotalDTO row : totals) {
            if (row.paymentMethod() == PaymentMethod.MERCADO_PAGO) {
                mpTotal = mpTotal.add(row.total());
            } else {
                cashTotal = cashTotal.add(row.total()); // Una venta sin medio de pago registrado cuenta como efectivo
            }
        }

        return new DailySalesSummaryDTO(
                cashTotal,
                mpTotal,
                cashTotal.add(mpTotal)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopSellerDTO> getTopSellers() {
        return saleRepository.findTopSellers(PageRequest.of(0, 5));
    }

    @Override
    public SaleResponseDTO voidSale(Long saleId, String reason, String adminUsername) {
        Sale sale = saleRepository.getRequiredById(saleId);
        LocalDate today = LocalDate.now(clock.withZone(businessZone));
        if (!sale.getDateTime().atZone(businessZone).toLocalDate().equals(today)) {
            throw new SaleNotVoidableException(saleId);
        }
        // Se leen antes de marcar la venta: el UPDATE limpia el contexto y los ítems ya no se podrían cargar.
        List<SaleDetail> details = List.copyOf(sale.getDetails());
        User admin = userRepository.getRequiredByUsername(adminUsername);

        if (saleRepository.markVoidedIfActive(saleId, Instant.now(clock), admin, reason.strip()) == 0) {
            throw new SaleAlreadyVoidedException(saleId);
        }
        for (SaleDetail detail : details) {
            productRepository.increaseStock(detail.getProduct().getId(), detail.getQuantity());
        }
        return saleMapper.toDTO(saleRepository.getRequiredById(saleId));
    }

    /** Instante en que empieza ese día en la zona del negocio (00:00 de Argentina). */
    private Instant startOf(LocalDate date) {
        return date.atStartOfDay(businessZone).toInstant();
    }
}
