package com.hospitality.operations.domain.restaurant.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.hospitality.operations.domain.restaurant.table.DiningTable;
import com.hospitality.operations.domain.restaurant.table.DiningTableRepository;
import com.hospitality.operations.domain.room.Room;
import com.hospitality.operations.domain.room.RoomRepository;
import com.hospitality.operations.exception.ResourceNotFoundException;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillServiceImpl implements BillService {

    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.08875");
    private static final AtomicLong invoiceCounter = new AtomicLong(0);

    private final BillRepository billRepository;
    private final BillLineItemRepository billLineItemRepository;
    private final RestaurantOrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RoomRepository roomRepository;
    private final DiningTableRepository diningTableRepository;
    private final InvoiceDescriptionService invoiceDescriptionService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public BillResponseDto generateBill(BillRequestDto requestDto) {
        List<BillResponseDto.LineItemDto> lineItemDtos = new ArrayList<>();
        List<String> descriptionParts = new ArrayList<>();
        String orderReference = null;
        Long orderId = requestDto.getOrderId();
        Long tableId = null;
        BigDecimal subtotal = BigDecimal.ZERO;

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
                BillResponseDto.LineItemDto li = BillResponseDto.LineItemDto.builder()
                        .itemType("ORDER_ITEM")
                        .itemId(item.getId())
                        .quantity(item.getQuantity())
                        .description(itemName)
                        .unitPrice(item.getUnitPrice())
                        .price(item.getSubtotal())
                        .build();
                lineItemDtos.add(li);
                descriptionParts.add(item.getQuantity() + "x " + itemName);
                subtotal = subtotal.add(item.getSubtotal());
            }
        }

        // 1b. Selected rooms (from card selection)
        List<Long> selectedRoomIds = requestDto.getSelectedRoomIds();
        if (selectedRoomIds != null && !selectedRoomIds.isEmpty()) {
            for (Long roomId : selectedRoomIds) {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
                BigDecimal total = room.getRatePerNight();
                BillResponseDto.LineItemDto li = BillResponseDto.LineItemDto.builder()
                        .itemType("ROOM")
                        .itemId(roomId)
                        .quantity(1)
                        .description("RM " + room.getRoomNumber() + " \u2014 " + room.getType() + " (1 NT)")
                        .unitPrice(room.getRatePerNight())
                        .price(total)
                        .build();
                lineItemDtos.add(li);
                descriptionParts.add("1 NT RM " + room.getRoomNumber());
                subtotal = subtotal.add(total);
            }
        }

        // 1c. Legacy room items (roomId + nights)
        List<BillRequestDto.RoomBillItem> roomItems = requestDto.getRoomItems();
        if (roomItems != null && !roomItems.isEmpty()) {
            for (BillRequestDto.RoomBillItem ri : roomItems) {
                if (selectedRoomIds != null && selectedRoomIds.contains(ri.getRoomId())) continue;
                Room room = roomRepository.findById(ri.getRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Room", "id", ri.getRoomId()));
                int nights = ri.getNights() != null ? ri.getNights() : 1;
                BigDecimal total = room.getRatePerNight().multiply(BigDecimal.valueOf(nights));
                BillResponseDto.LineItemDto li = BillResponseDto.LineItemDto.builder()
                        .itemType("ROOM")
                        .itemId(ri.getRoomId())
                        .quantity(nights)
                        .description("RM " + room.getRoomNumber() + " \u2014 " + room.getType() + " (" + nights + " NT)")
                        .unitPrice(room.getRatePerNight())
                        .price(total)
                        .build();
                lineItemDtos.add(li);
                descriptionParts.add(nights + " NT RM " + room.getRoomNumber());
                subtotal = subtotal.add(total);
            }
        }

        // 1d. Selected tables (from card selection)
        List<Long> selectedTableIds = requestDto.getSelectedTableIds();
        if (selectedTableIds != null && !selectedTableIds.isEmpty()) {
            for (Long tid : selectedTableIds) {
                DiningTable dt = diningTableRepository.findById(tid)
                        .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", tid));
                BigDecimal coverCharge = BigDecimal.ZERO;
                BillResponseDto.LineItemDto li = BillResponseDto.LineItemDto.builder()
                        .itemType("TABLE")
                        .itemId(tid)
                        .quantity(1)
                        .description("TABLE " + dt.getTableNumber())
                        .unitPrice(coverCharge)
                        .price(coverCharge)
                        .build();
                lineItemDtos.add(li);
                descriptionParts.add("Table " + dt.getTableNumber());
                if (tableId == null) tableId = tid;
            }
        }

        // 1e. Selected menu items (from card selection)
        List<Long> selectedMenuItemIds = requestDto.getSelectedMenuItemIds();
        if (selectedMenuItemIds != null && !selectedMenuItemIds.isEmpty()) {
            List<MenuItem> menuItems = menuItemRepository.findAllById(selectedMenuItemIds);
            for (MenuItem mi : menuItems) {
                BillResponseDto.LineItemDto li = BillResponseDto.LineItemDto.builder()
                        .itemType("MENU_ITEM")
                        .itemId(mi.getId())
                        .quantity(1)
                        .description(mi.getName())
                        .unitPrice(mi.getPrice())
                        .price(mi.getPrice())
                        .build();
                lineItemDtos.add(li);
                descriptionParts.add("1x " + mi.getName());
                subtotal = subtotal.add(mi.getPrice());
            }
        }

        // 1f. Custom additional items
        List<AdditionalItemDto> additionalItems = requestDto.getAdditionalItems();
        if (additionalItems != null) {
            for (AdditionalItemDto item : additionalItems) {
                BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BillResponseDto.LineItemDto li = BillResponseDto.LineItemDto.builder()
                        .itemType("CUSTOM")
                        .itemId(null)
                        .quantity(item.getQuantity())
                        .description(item.getDescription())
                        .unitPrice(item.getUnitPrice())
                        .price(total)
                        .build();
                lineItemDtos.add(li);
                descriptionParts.add(item.getQuantity() + "x " + item.getDescription());
                subtotal = subtotal.add(total);
            }
        }

        // 2. Calculate tax and discount
        BigDecimal taxRate = DEFAULT_TAX_RATE;
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = requestDto.getDiscount() != null
                ? requestDto.getDiscount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal totalDue = subtotal.add(taxAmount).subtract(discount).setScale(2, RoundingMode.HALF_UP);

        // 3. Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        // 4. Generate description
        String ref = orderReference != null ? orderReference : invoiceNumber;
        String aiDescription = invoiceDescriptionService.generateDescription(ref, descriptionParts);

        // 5. Build and save bill
        Bill bill = Bill.builder()
                .orderId(orderId)
                .orderReference(orderReference)
                .tableId(tableId)
                .serverName(requestDto.getServerName())
                .customerName(requestDto.getCustomerName())
                .phoneNumber(requestDto.getPhoneNumber())
                .email(requestDto.getEmail())
                .notes(requestDto.getNotes())
                .invoiceNumber(invoiceNumber)
                .status(BillStatus.PAID)
                .subtotal(subtotal)
                .taxRate(taxRate)
                .taxAmount(taxAmount)
                .discount(discount)
                .totalDue(totalDue)
                .aiDescription(aiDescription)
                .tenantSchema(requestDto.getTenantSchema() != null ? requestDto.getTenantSchema() : "default")
                .build();

        Bill saved = billRepository.save(bill);

        // 6. Persist line items
        List<BillLineItem> persistedItems = new ArrayList<>();
        for (BillResponseDto.LineItemDto liDto : lineItemDtos) {
            BillLineItem bli = BillLineItem.builder()
                    .bill(saved)
                    .itemType(liDto.getItemType())
                    .itemId(liDto.getItemId())
                    .description(liDto.getDescription())
                    .quantity(liDto.getQuantity())
                    .unitPrice(liDto.getUnitPrice())
                    .totalPrice(liDto.getPrice())
                    .tenantSchema(saved.getTenantSchema())
                    .build();
            persistedItems.add(bli);
        }
        billLineItemRepository.saveAll(persistedItems);

        return BillMapper.toDto(saved, lineItemDtos);
    }

    @Override
    @Transactional
    public BillResponseDto flagInvoice(Long id, String note) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));
        bill.setFlagged(!Boolean.TRUE.equals(bill.getFlagged()));
        bill.setFlagNote(note);
        Bill saved = billRepository.save(bill);
        List<BillResponseDto.LineItemDto> lineItems = buildLineItemsFromDb(saved);
        BillResponseDto dto = BillMapper.toDto(saved, lineItems);
        enrichTableNumber(dto);
        return dto;
    }

    @Override
    @Transactional
    public BillResponseDto updateBill(Long id, BillRequestDto requestDto) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));

        bill.setOrderId(requestDto.getOrderId());
        bill.setServerName(requestDto.getServerName());
        bill.setCustomerName(requestDto.getCustomerName());
        bill.setPhoneNumber(requestDto.getPhoneNumber());
        bill.setEmail(requestDto.getEmail());
        bill.setNotes(requestDto.getNotes());
        if (requestDto.getDiscount() != null) {
            bill.setDiscount(requestDto.getDiscount());
        }
        if (requestDto.getStatus() != null) {
            bill.setStatus(requestDto.getStatus());
        }

        // Recalculate financials
        BigDecimal subtotal = BigDecimal.ZERO;
        List<BillResponseDto.LineItemDto> lineItemDtos = new ArrayList<>();

        // Additional items
        List<BillRequestDto.AdditionalItemDto> additionalItems = requestDto.getAdditionalItems();
        if (additionalItems != null) {
            for (BillRequestDto.AdditionalItemDto item : additionalItems) {
                BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BillResponseDto.LineItemDto li = BillResponseDto.LineItemDto.builder()
                        .itemType("CUSTOM").itemId(null)
                        .quantity(item.getQuantity())
                        .description(item.getDescription())
                        .unitPrice(item.getUnitPrice())
                        .price(total).build();
                lineItemDtos.add(li);
                subtotal = subtotal.add(total);
            }
        }

        // If no additional items, keep original line items
        if (lineItemDtos.isEmpty()) {
            subtotal = bill.getSubtotal();
        } else {
            BigDecimal taxAmount = subtotal.multiply(bill.getTaxRate()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalDue = subtotal.add(taxAmount).subtract(bill.getDiscount()).setScale(2, RoundingMode.HALF_UP);
            bill.setSubtotal(subtotal);
            bill.setTaxAmount(taxAmount);
            bill.setTotalDue(totalDue);

            billLineItemRepository.deleteByBillId(bill.getId());
            entityManager.flush();
            List<BillLineItem> newItems = lineItemDtos.stream().map(liDto ->
                BillLineItem.builder()
                        .bill(bill)
                        .itemType(liDto.getItemType())
                        .itemId(liDto.getItemId())
                        .description(liDto.getDescription())
                        .quantity(liDto.getQuantity())
                        .unitPrice(liDto.getUnitPrice())
                        .totalPrice(liDto.getPrice())
                        .tenantSchema(bill.getTenantSchema())
                        .build()
            ).toList();
            billLineItemRepository.saveAll(newItems);
        }

        Bill saved = billRepository.save(bill);
        List<BillResponseDto.LineItemDto> updatedLineItems = buildLineItemsFromDb(saved);
        BillResponseDto dto = BillMapper.toDto(saved, updatedLineItems);
        enrichTableNumber(dto);
        return dto;
    }

    @Override
    public BillResponseDto getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));
        List<BillResponseDto.LineItemDto> lineItems = buildLineItemsFromDb(bill);
        BillResponseDto dto = BillMapper.toDto(bill, lineItems);
        enrichTableNumber(dto);
        return dto;
    }

    @Override
    public BillResponseDto getBillByOrderId(Long orderId) {
        Bill bill = billRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "orderId", orderId));
        List<BillResponseDto.LineItemDto> lineItems = buildLineItemsFromDb(bill);
        BillResponseDto dto = BillMapper.toDto(bill, lineItems);
        enrichTableNumber(dto);
        return dto;
    }

    @Override
    public List<BillResponseDto> getAllBills() {
        return billRepository.findAll().stream()
                .map(bill -> {
                    List<BillResponseDto.LineItemDto> lineItems = buildLineItemsFromDb(bill);
                    BillResponseDto dto = BillMapper.toDto(bill, lineItems);
                    enrichTableNumber(dto);
                    return dto;
                })
                .toList();
    }

    @Override
    public Page<BillResponseDto> getBills(Instant dateFrom, Instant dateTo, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean isUserOrManager = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(r -> r.equals("ROLE_USER") || r.equals("ROLE_MANAGER"));
            if (isUserOrManager) {
                Instant sevenDaysAgo = Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS);
                if (dateFrom == null || dateFrom.isBefore(sevenDaysAgo)) {
                    dateFrom = sevenDaysAgo;
                }
            }
        }
        Page<Bill> billPage;
        if (dateFrom != null && dateTo != null) {
            billPage = billRepository.findAllByCreatedAtBetween(dateFrom, dateTo, pageable);
        } else if (dateFrom != null) {
            billPage = billRepository.findAllByCreatedAtAfter(dateFrom, pageable);
        } else if (dateTo != null) {
            billPage = billRepository.findAllByCreatedAtBefore(dateTo, pageable);
        } else {
            billPage = billRepository.findAll(pageable);
        }
        return billPage.map(bill -> {
            List<BillResponseDto.LineItemDto> lineItems = buildLineItemsFromDb(bill);
            BillResponseDto dto = BillMapper.toDto(bill, lineItems);
            enrichTableNumber(dto);
            return dto;
        });
    }

    @Override
    public String exportCsv(Instant dateFrom, Instant dateTo) {
        List<Bill> bills;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (dateFrom != null && dateTo != null) {
            bills = billRepository.findAllByCreatedAtBetween(dateFrom, dateTo, sort);
        } else if (dateFrom != null) {
            bills = billRepository.findAllByCreatedAtAfter(dateFrom, sort);
        } else if (dateTo != null) {
            bills = billRepository.findAllByCreatedAtBefore(dateTo, sort);
        } else {
            bills = billRepository.findAll(sort);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Invoice#,Date,Server,Customer,Subtotal,Tax,Discount,Total,Status,Items\n");
        for (Bill bill : bills) {
            sb.append(escapeCsv(bill.getInvoiceNumber())).append(",");
            sb.append(bill.getCreatedAt() != null ? bill.getCreatedAt().toString() : "").append(",");
            sb.append(escapeCsv(bill.getServerName())).append(",");
            sb.append(escapeCsv(bill.getCustomerName())).append(",");
            sb.append(bill.getSubtotal()).append(",");
            sb.append(bill.getTaxAmount()).append(",");
            sb.append(bill.getDiscount()).append(",");
            sb.append(bill.getTotalDue()).append(",");
            sb.append(bill.getStatus()).append(",");
            sb.append(escapeCsv(bill.getAiDescription())).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private List<BillResponseDto.LineItemDto> buildLineItemsFromDb(Bill bill) {
        return billLineItemRepository.findByBillId(bill.getId()).stream()
                .map(bli -> BillResponseDto.LineItemDto.builder()
                        .id(bli.getId())
                        .itemType(bli.getItemType())
                        .itemId(bli.getItemId())
                        .quantity(bli.getQuantity())
                        .description(bli.getDescription())
                        .unitPrice(bli.getUnitPrice())
                        .price(bli.getTotalPrice())
                        .build())
                .toList();
    }

    private void enrichTableNumber(BillResponseDto dto) {
        if (dto.getTableId() != null) {
            diningTableRepository.findById(dto.getTableId())
                    .ifPresent(t -> dto.setTableNumber(t.getTableNumber()));
        }
    }

    private String generateInvoiceNumber() {
        String datePart = LocalDate.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = invoiceCounter.incrementAndGet() % 10000;
        return "INV-" + datePart + "-" + String.format("%04d", seq);
    }
}
