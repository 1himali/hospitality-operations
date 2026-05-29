package com.hospitality.operations.domain.restaurant.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.domain.restaurant.menu.MenuItem;
import com.hospitality.operations.domain.restaurant.menu.MenuItemRepository;
import com.hospitality.operations.domain.restaurant.order.dto.OrderItemDto;
import com.hospitality.operations.domain.restaurant.order.dto.OrderRequestDto;
import com.hospitality.operations.domain.restaurant.order.dto.OrderResponseDto;
import com.hospitality.operations.domain.restaurant.order.mapper.OrderMapper;
import com.hospitality.operations.domain.restaurant.table.DiningTable;
import com.hospitality.operations.domain.restaurant.table.DiningTableRepository;
import com.hospitality.operations.domain.restaurant.table.TableStatus;
import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantOrderServiceImpl implements RestaurantOrderService {

    private final RestaurantOrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final DiningTableRepository diningTableRepository;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        String reference = requestDto.getOrderReference();
        if (reference == null || reference.isBlank()) {
            reference = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        RestaurantOrder order = RestaurantOrder.builder()
                .orderReference(reference)
                .tableId(requestDto.getTableId())
                .status(OrderStatus.NEW)
                .tenantSchema(requestDto.getTenantSchema() != null ? requestDto.getTenantSchema() : "default")
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDto itemDto : requestDto.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemDto.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemDto.getMenuItemId()));
            BigDecimal unitPrice = menuItem.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            total = total.add(subtotal);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .menuItemId(menuItem.getId())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            order.getItems().add(item);
        }

        order.setTotalAmount(total);

        if (requestDto.getTableId() != null) {
            diningTableRepository.findById(requestDto.getTableId()).ifPresent(table -> {
                table.setCurrentOrderId(order.getId());
                table.setStatus(TableStatus.OCCUPIED);
                diningTableRepository.save(table);
            });
        }

        RestaurantOrder saved = orderRepository.save(order);
        return buildResponse(saved);
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        RestaurantOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantOrder", "id", id));
        return buildResponse(order);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderRequestDto requestDto) {
        RestaurantOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantOrder", "id", id));

        if (requestDto.getTableId() != null) {
            order.setTableId(requestDto.getTableId());
        }

        if (requestDto.getItems() != null && !requestDto.getItems().isEmpty()) {
            order.getItems().clear();
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItemDto itemDto : requestDto.getItems()) {
                MenuItem menuItem = menuItemRepository.findById(itemDto.getMenuItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemDto.getMenuItemId()));
                BigDecimal unitPrice = menuItem.getPrice();
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                total = total.add(subtotal);

                OrderItem item = OrderItem.builder()
                        .order(order)
                        .menuItemId(menuItem.getId())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(unitPrice)
                        .subtotal(subtotal)
                        .build();
                order.getItems().add(item);
            }
            order.setTotalAmount(total);
        }

        RestaurantOrder saved = orderRepository.save(order);
        return buildResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long id, String status) {
        RestaurantOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantOrder", "id", id));
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED) {
            if (order.getTableId() != null) {
                diningTableRepository.findById(order.getTableId()).ifPresent(table -> {
                    table.setCurrentOrderId(null);
                    table.setStatus(TableStatus.AVAILABLE);
                    diningTableRepository.save(table);
                });
            }
        }

        RestaurantOrder saved = orderRepository.save(order);
        return buildResponse(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        RestaurantOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantOrder", "id", id));

        if (order.getTableId() != null) {
            diningTableRepository.findById(order.getTableId()).ifPresent(table -> {
                table.setCurrentOrderId(null);
                table.setStatus(TableStatus.AVAILABLE);
                diningTableRepository.save(table);
            });
        }

        orderRepository.delete(order);
    }

    @Override
    public List<OrderResponseDto> getAllOrders(String status, Instant dateFrom, Instant dateTo) {
        List<RestaurantOrder> orders;

        if (status != null && !status.isEmpty() && dateFrom != null && dateTo != null) {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                List<OrderStatus> activeStatuses = List.of(OrderStatus.NEW, OrderStatus.PREPARING, OrderStatus.READY);
                orders = orderRepository.findByCreatedAtBetween(dateFrom, dateTo).stream()
                        .filter(o -> activeStatuses.contains(o.getStatus()))
                        .toList();
            } else {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatusAndCreatedAtBetween(orderStatus, dateFrom, dateTo);
            }
        } else if (status != null && !status.isEmpty()) {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                List<OrderStatus> activeStatuses = List.of(OrderStatus.NEW, OrderStatus.PREPARING, OrderStatus.READY);
                orders = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                        .filter(o -> activeStatuses.contains(o.getStatus()))
                        .toList();
            } else {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatusOrderByCreatedAtDesc(orderStatus);
            }
        } else if (dateFrom != null && dateTo != null) {
            orders = orderRepository.findByCreatedAtBetween(dateFrom, dateTo);
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        }

        return orders.stream()
                .map(this::buildResponse)
                .toList();
    }

    private OrderResponseDto buildResponse(RestaurantOrder order) {
        String tableNumber = null;
        if (order.getTableId() != null) {
            tableNumber = diningTableRepository.findById(order.getTableId())
                    .map(DiningTable::getTableNumber)
                    .orElse(null);
        }

        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> {
                    String itemName = menuItemRepository.findById(item.getMenuItemId())
                            .map(MenuItem::getName)
                            .orElse("Item #" + item.getMenuItemId());
                    return OrderMapper.toItemDto(item, itemName);
                })
                .toList();

        return OrderMapper.toDto(order, tableNumber, itemDtos);
    }
}
