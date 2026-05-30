package com.hospitality.operations.domain.restaurant.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.hospitality.operations.ai.InvoiceDescriptionService;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;
import com.hospitality.operations.domain.restaurant.menu.MenuItemRepository;
import com.hospitality.operations.domain.restaurant.order.RestaurantOrderRepository;
import com.hospitality.operations.domain.restaurant.table.DiningTableRepository;
import com.hospitality.operations.domain.room.RoomRepository;
import com.hospitality.operations.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillLineItemRepository billLineItemRepository;

    @Mock
    private RestaurantOrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DiningTableRepository diningTableRepository;

    @Mock
    private InvoiceDescriptionService invoiceDescriptionService;

    private BillServiceImpl billService;

    @BeforeEach
    void setUp() {
        billService = new BillServiceImpl(billRepository, billLineItemRepository, orderRepository,
                menuItemRepository, roomRepository, diningTableRepository, invoiceDescriptionService);
    }

    @Test
    void getBillById_shouldReturnBill() {
        Bill bill = Bill.builder()
                .id(1L).orderReference("ORD-001")
                .invoiceNumber("INV-20260530-0001")
                .status(BillStatus.PAID)
                .subtotal(new BigDecimal("100.00"))
                .taxRate(new BigDecimal("0.08875"))
                .taxAmount(new BigDecimal("8.88"))
                .discount(BigDecimal.ZERO)
                .totalDue(new BigDecimal("108.88"))
                .tenantSchema("default")
                .createdAt(Instant.now())
                .build();

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billLineItemRepository.findByBillId(1L)).thenReturn(List.of());

        BillResponseDto response = billService.getBillById(1L);

        assertNotNull(response);
        assertEquals("ORD-001", response.getOrderReference());
        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
        assertEquals("INV-20260530-0001", response.getInvoiceNumber());
        assertEquals(BillStatus.PAID, response.getStatus());
    }

    @Test
    void getBillById_shouldThrowWhenNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> billService.getBillById(999L));
    }

    @Test
    void getBills_shouldReturnPaginatedResults() {
        Bill bill = Bill.builder()
                .id(1L).orderReference("ORD-001")
                .invoiceNumber("INV-20260530-0001")
                .status(BillStatus.PAID)
                .subtotal(new BigDecimal("100.00"))
                .taxRate(new BigDecimal("0.08875"))
                .taxAmount(new BigDecimal("8.88"))
                .discount(BigDecimal.ZERO)
                .totalDue(new BigDecimal("108.88"))
                .tenantSchema("default")
                .createdAt(Instant.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(billRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(bill), pageable, 1));
        when(billLineItemRepository.findByBillId(1L)).thenReturn(List.of());

        Page<BillResponseDto> result = billService.getBills(null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("ORD-001", result.getContent().get(0).getOrderReference());
        verify(billRepository).findAll(pageable);
    }

    @Test
    void getBills_shouldFilterByDateRange() {
        Instant now = Instant.now();
        Instant from = now.minusSeconds(86400);
        Instant to = now.plusSeconds(86400);

        Bill bill = Bill.builder()
                .id(1L).orderReference("ORD-001")
                .invoiceNumber("INV-20260530-0001")
                .status(BillStatus.PAID)
                .subtotal(new BigDecimal("50.00"))
                .taxRate(new BigDecimal("0.08875"))
                .taxAmount(new BigDecimal("4.44"))
                .discount(BigDecimal.ZERO)
                .totalDue(new BigDecimal("54.44"))
                .tenantSchema("default")
                .createdAt(now)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(billRepository.findAllByCreatedAtBetween(from, to, pageable))
                .thenReturn(new PageImpl<>(List.of(bill), pageable, 1));
        when(billLineItemRepository.findByBillId(1L)).thenReturn(List.of());

        Page<BillResponseDto> result = billService.getBills(from, to, pageable);

        assertEquals(1, result.getTotalElements());
        verify(billRepository).findAllByCreatedAtBetween(from, to, pageable);
    }

    @Test
    void getBills_shouldFilterByDateFromOnly() {
        Instant from = Instant.now().minusSeconds(86400);
        Bill bill = Bill.builder()
                .id(2L).orderReference("ORD-002")
                .invoiceNumber("INV-20260530-0002")
                .status(BillStatus.PAID)
                .subtotal(new BigDecimal("75.00"))
                .taxRate(new BigDecimal("0.08875"))
                .taxAmount(new BigDecimal("6.66"))
                .discount(BigDecimal.ZERO)
                .totalDue(new BigDecimal("81.66"))
                .tenantSchema("default")
                .createdAt(Instant.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(billRepository.findAllByCreatedAtAfter(from, pageable))
                .thenReturn(new PageImpl<>(List.of(bill), pageable, 1));
        when(billLineItemRepository.findByBillId(2L)).thenReturn(List.of());

        Page<BillResponseDto> result = billService.getBills(from, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(billRepository).findAllByCreatedAtAfter(from, pageable);
    }

    @Test
    void getBills_shouldFilterByDateToOnly() {
        Instant to = Instant.now().plusSeconds(86400);
        Bill bill = Bill.builder()
                .id(3L).orderReference("ORD-003")
                .invoiceNumber("INV-20260530-0003")
                .status(BillStatus.PAID)
                .subtotal(new BigDecimal("200.00"))
                .taxRate(new BigDecimal("0.08875"))
                .taxAmount(new BigDecimal("17.75"))
                .discount(new BigDecimal("10.00"))
                .totalDue(new BigDecimal("207.75"))
                .tenantSchema("default")
                .createdAt(Instant.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(billRepository.findAllByCreatedAtBefore(to, pageable))
                .thenReturn(new PageImpl<>(List.of(bill), pageable, 1));
        when(billLineItemRepository.findByBillId(3L)).thenReturn(List.of());

        Page<BillResponseDto> result = billService.getBills(null, to, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal("10.00"), result.getContent().get(0).getDiscount());
        assertEquals("INV-20260530-0003", result.getContent().get(0).getInvoiceNumber());
        verify(billRepository).findAllByCreatedAtBefore(to, pageable);
    }
}
