package com.hospitality.operations.domain.restaurant.order;

import java.time.Instant;
import java.util.List;

import com.hospitality.operations.domain.restaurant.order.dto.OrderRequestDto;
import com.hospitality.operations.domain.restaurant.order.dto.OrderResponseDto;

public interface RestaurantOrderService {

    OrderResponseDto createOrder(OrderRequestDto requestDto);

    OrderResponseDto getOrderById(Long id);

    OrderResponseDto updateOrder(Long id, OrderRequestDto requestDto);

    OrderResponseDto updateOrderStatus(Long id, String status);

    void deleteOrder(Long id);

    List<OrderResponseDto> getAllOrders(String status, Instant dateFrom, Instant dateTo);
}
