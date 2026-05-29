package com.hospitality.operations.domain.restaurant.order.mapper;

import java.util.List;

import com.hospitality.operations.domain.restaurant.order.OrderItem;
import com.hospitality.operations.domain.restaurant.order.RestaurantOrder;
import com.hospitality.operations.domain.restaurant.order.dto.OrderItemDto;
import com.hospitality.operations.domain.restaurant.order.dto.OrderResponseDto;

public final class OrderMapper {

    private OrderMapper() {
        // utility class
    }

    public static OrderItemDto toItemDto(OrderItem item, String menuItemName) {
        return OrderItemDto.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .menuItemName(menuItemName)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    public static OrderItemDto toItemDto(OrderItem item) {
        return toItemDto(item, null);
    }

    public static OrderResponseDto toDto(RestaurantOrder order, String tableNumber, List<OrderItemDto> items) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .orderReference(order.getOrderReference())
                .tableId(order.getTableId())
                .tableNumber(tableNumber)
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .tenantSchema(order.getTenantSchema())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }
}
