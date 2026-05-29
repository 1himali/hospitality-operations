package com.hospitality.operations.domain.restaurant.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.ai.InvoiceDescriptionService;
import com.hospitality.operations.domain.restaurant.billing.dto.BillRequestDto;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;
import com.hospitality.operations.domain.restaurant.billing.mapping.BillMapper;
import com.hospitality.operations.domain.restaurant.menu.MenuItem;
import com.hospitality.operations.domain.restaurant.menu.MenuItemRepository;
import com.hospitality.operations.domain.restaurant.order.OrderItem;
import com.hospitality.operations.domain.restaurant.order.RestaurantOrder;
import com.hospitality.operations.domain.restaurant.order.RestaurantOrderRepository;
import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillServiceImpl implements BillService {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.08875");

    private final BillRepository billRepository;
    private final RestaurantOrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final InvoiceDescriptionService invoiceDescriptionService;

    @Override
    @Transactional
    public BillResponseDto generateBill(BillRequestDto requestDto) {
        // 1. Fetch the order
        RestaurantOrder order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantOrder", "id", requestDto.getOrderId()));

        // 2. Build line items and calculate subtotal
        List<OrderItem> orderItems = order.getItems();
        List<BillResponseDto.LineItemDto> lineItems = orderItems.stream()
                .map(item -> {
                    String itemName = menuItemRepository.findById(item.getMenuItemId())
                            .map(MenuItem::getName)
                            .orElse("Item #" + item.getMenuItemId());
                    return BillResponseDto.LineItemDto.builder()
                            .quantity(item.getQuantity())
                            .description(itemName)
                            .unitPrice(item.getUnitPrice())
                            .price(item.getSubtotal())
                            .build();
                })
                .toList();

        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Calculate tax and discount
        BigDecimal taxRate = DEFAULT_TAX_RATE;
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = requestDto.getDiscount() != null
                ? requestDto.getDiscount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal totalDue = subtotal.add(taxAmount).subtract(discount).setScale(2, RoundingMode.HALF_UP);

        // 4. Generate AI description
        List<String> itemDescriptions = lineItems.stream()
                .map(li -> li.getQuantity() + "x " + li.getDescription())
                .toList();
        String aiDescription = invoiceDescriptionService.generateDescription(
                order.getOrderReference(), itemDescriptions);

        // 5. Build and save bill
        Bill bill = Bill.builder()
                .orderId(order.getId())
                .orderReference(order.getOrderReference())
                .tableId(order.getTableId())
                .serverName(requestDto.getServerName())
                .subtotal(subtotal)
                .taxRate(taxRate)
                .taxAmount(taxAmount)
                .discount(discount)
                .totalDue(totalDue)
                .aiDescription(aiDescription)
                .tenantSchema(requestDto.getTenantSchema() != null ? requestDto.getTenantSchema() : "default")
                .build();

        Bill saved = billRepository.save(bill);
        return BillMapper.toDto(saved, lineItems);
    }

    @Override
    public BillResponseDto getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));
        List<BillResponseDto.LineItemDto> lineItems = buildLineItems(bill.getOrderId());
        return BillMapper.toDto(bill, lineItems);
    }

    @Override
    public BillResponseDto getBillByOrderId(Long orderId) {
        Bill bill = billRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "orderId", orderId));
        List<BillResponseDto.LineItemDto> lineItems = buildLineItems(orderId);
        return BillMapper.toDto(bill, lineItems);
    }

    @Override
    public List<BillResponseDto> getAllBills() {
        return billRepository.findAll().stream()
                .map(bill -> {
                    List<BillResponseDto.LineItemDto> lineItems = buildLineItems(bill.getOrderId());
                    return BillMapper.toDto(bill, lineItems);
                })
                .toList();
    }

    private List<BillResponseDto.LineItemDto> buildLineItems(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getItems().stream()
                        .map(item -> {
                            String itemName = menuItemRepository.findById(item.getMenuItemId())
                                    .map(MenuItem::getName)
                                    .orElse("Item #" + item.getMenuItemId());
                            return BillResponseDto.LineItemDto.builder()
                                    .quantity(item.getQuantity())
                                    .description(itemName)
                                    .unitPrice(item.getUnitPrice())
                                    .price(item.getSubtotal())
                                    .build();
                        })
                        .toList())
                .orElse(List.of());
    }
}
