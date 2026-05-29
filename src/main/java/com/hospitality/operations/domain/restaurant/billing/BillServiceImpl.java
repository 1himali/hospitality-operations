package com.hospitality.operations.domain.restaurant.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.ai.InvoiceDescriptionService;
import com.hospitality.operations.domain.restaurant.billing.dto.BillRequestDto;
import com.hospitality.operations.domain.restaurant.billing.dto.BillRequestDto.AdditionalItemDto;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;
import com.hospitality.operations.domain.restaurant.billing.mapping.BillMapper;
import com.hospitality.operations.domain.restaurant.menu.MenuItem;
import com.hospitality.operations.domain.restaurant.menu.MenuItemRepository;
import com.hospitality.operations.domain.restaurant.order.OrderItem;
import com.hospitality.operations.domain.restaurant.order.RestaurantOrder;
import com.hospitality.operations.domain.restaurant.order.RestaurantOrderRepository;
import com.hospitality.operations.domain.room.Room;
import com.hospitality.operations.domain.room.RoomRepository;
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
    private final RoomRepository roomRepository;
    private final InvoiceDescriptionService invoiceDescriptionService;

    @Override
    @Transactional
    public BillResponseDto generateBill(BillRequestDto requestDto) {
        // 1. Gather line items from all sources
        List<BillResponseDto.LineItemDto> lineItems = new ArrayList<>();
        List<String> descriptionParts = new ArrayList<>();
        String orderReference = null;
        Long orderId = requestDto.getOrderId();
        Long tableId = null;

        // 1a. Restaurant order items
        if (orderId != null) {
            RestaurantOrder order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("RestaurantOrder", "id", orderId));
            orderReference = order.getOrderReference();
            tableId = order.getTableId();
            for (OrderItem item : order.getItems()) {
                String itemName = menuItemRepository.findById(item.getMenuItemId())
                        .map(MenuItem::getName)
                        .orElse("Item #" + item.getMenuItemId());
                lineItems.add(BillResponseDto.LineItemDto.builder()
                        .quantity(item.getQuantity())
                        .description(itemName)
                        .unitPrice(item.getUnitPrice())
                        .price(item.getSubtotal())
                        .build());
                descriptionParts.add(item.getQuantity() + "x " + itemName);
            }
        }

        // 1b. Room charges
        List<BillRequestDto.RoomBillItem> roomItems = requestDto.getRoomItems();
        if (roomItems != null && !roomItems.isEmpty()) {
            for (BillRequestDto.RoomBillItem ri : roomItems) {
                Room room = roomRepository.findById(ri.getRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Room", "id", ri.getRoomId()));
                int nights = ri.getNights() != null ? ri.getNights() : 1;
                BigDecimal total = room.getRatePerNight().multiply(BigDecimal.valueOf(nights));
                lineItems.add(BillResponseDto.LineItemDto.builder()
                        .quantity(nights)
                        .description("RM " + room.getRoomNumber() + " — " + room.getType() + " (" + nights + " NT)")
                        .unitPrice(room.getRatePerNight())
                        .price(total)
                        .build());
                descriptionParts.add(nights + " NT RM " + room.getRoomNumber());
            }
        }

        // 1c. Custom items
        List<AdditionalItemDto> additionalItems = requestDto.getAdditionalItems();
        if (additionalItems != null) {
            for (AdditionalItemDto item : additionalItems) {
                BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                lineItems.add(BillResponseDto.LineItemDto.builder()
                        .quantity(item.getQuantity())
                        .description(item.getDescription())
                        .unitPrice(item.getUnitPrice())
                        .price(total)
                        .build());
                descriptionParts.add(item.getQuantity() + "x " + item.getDescription());
            }
        }

        // 2. Calculate subtotal
        BigDecimal subtotal = lineItems.stream()
                .map(BillResponseDto.LineItemDto::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Calculate tax and discount
        BigDecimal taxRate = DEFAULT_TAX_RATE;
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = requestDto.getDiscount() != null
                ? requestDto.getDiscount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal totalDue = subtotal.add(taxAmount).subtract(discount).setScale(2, RoundingMode.HALF_UP);

        // 4. Generate description
        String ref = orderReference != null ? orderReference : "INV";
        String aiDescription = invoiceDescriptionService.generateDescription(ref, descriptionParts);

        // 5. Build and save bill
        Bill bill = Bill.builder()
                .orderId(orderId)
                .orderReference(orderReference)
                .tableId(tableId)
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
        if (orderId == null) return List.of();
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
