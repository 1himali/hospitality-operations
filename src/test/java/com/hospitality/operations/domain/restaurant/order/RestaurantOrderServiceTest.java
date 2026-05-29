package com.hospitality.operations.domain.restaurant.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.hospitality.operations.domain.restaurant.menu.MenuItem;
import com.hospitality.operations.domain.restaurant.menu.MenuItemRepository;
import com.hospitality.operations.domain.restaurant.order.dto.OrderItemDto;
import com.hospitality.operations.domain.restaurant.order.dto.OrderRequestDto;
import com.hospitality.operations.domain.restaurant.order.dto.OrderResponseDto;
import com.hospitality.operations.domain.restaurant.table.DiningTable;
import com.hospitality.operations.domain.restaurant.table.DiningTableRepository;
import com.hospitality.operations.domain.restaurant.table.TableStatus;
import com.hospitality.operations.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class RestaurantOrderServiceTest {

    @Mock
    private RestaurantOrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private DiningTableRepository diningTableRepository;

    private RestaurantOrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new RestaurantOrderServiceImpl(orderRepository, menuItemRepository, diningTableRepository);
    }

    @Test
    void createOrder_shouldCreateOrderWithItems() {
        MenuItem menuItem = MenuItem.builder()
                .id(1L).name("Burger").price(new BigDecimal("12.50")).available(true)
                .tenantSchema("default").build();

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        RestaurantOrder savedOrder = RestaurantOrder.builder()
                .id(1L).orderReference("ORD-TEST").status(OrderStatus.NEW)
                .totalAmount(new BigDecimal("25.00")).tenantSchema("default")
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
        when(orderRepository.save(any(RestaurantOrder.class))).thenReturn(savedOrder);

        OrderRequestDto request = OrderRequestDto.builder()
                .items(List.of(
                        OrderItemDto.builder().menuItemId(1L).quantity(2).build()
                ))
                .build();

        OrderResponseDto response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("ORD-TEST", response.getOrderReference());
        assertEquals("NEW", response.getStatus());
        verify(orderRepository).save(any(RestaurantOrder.class));
    }

    @Test
    void createOrder_shouldThrowWhenMenuItemNotFound() {
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        OrderRequestDto request = OrderRequestDto.builder()
                .items(List.of(
                        OrderItemDto.builder().menuItemId(999L).quantity(1).build()
                ))
                .build();

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        RestaurantOrder order = RestaurantOrder.builder()
                .id(1L).orderReference("ORD-001").status(OrderStatus.NEW)
                .totalAmount(new BigDecimal("10.00")).tenantSchema("default")
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponseDto response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals("ORD-001", response.getOrderReference());
    }

    @Test
    void getOrderById_shouldThrowWhenNotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void updateOrderStatus_shouldUpdateAndFreeTableOnComplete() {
        DiningTable table = DiningTable.builder()
                .id(1L).tableNumber("T1").status(TableStatus.OCCUPIED)
                .capacity(4).tenantSchema("default").build();

        RestaurantOrder order = RestaurantOrder.builder()
                .id(1L).orderReference("ORD-001").status(OrderStatus.PREPARING)
                .tableId(1L).totalAmount(new BigDecimal("10.00"))
                .tenantSchema("default").createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(orderRepository.save(any(RestaurantOrder.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponseDto response = orderService.updateOrderStatus(1L, "COMPLETED");

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        verify(diningTableRepository).save(table);
    }

    @Test
    void getAllOrders_shouldReturnAllWhenNoFilters() {
        RestaurantOrder order = RestaurantOrder.builder()
                .id(1L).orderReference("ORD-001").status(OrderStatus.NEW)
                .totalAmount(new BigDecimal("10.00")).tenantSchema("default")
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(order));

        List<OrderResponseDto> orders = orderService.getAllOrders(null, null, null);

        assertEquals(1, orders.size());
        assertEquals("ORD-001", orders.get(0).getOrderReference());
    }

    @Test
    void getAllOrders_shouldFilterByDateRange() {
        Instant now = Instant.now();
        Instant start = now.minusSeconds(3600);
        Instant end = now.plusSeconds(3600);

        RestaurantOrder order = RestaurantOrder.builder()
                .id(1L).orderReference("ORD-001").status(OrderStatus.NEW)
                .totalAmount(new BigDecimal("10.00")).tenantSchema("default")
                .createdAt(now).updatedAt(now).build();

        when(orderRepository.findByCreatedAtBetween(start, end)).thenReturn(List.of(order));

        List<OrderResponseDto> orders = orderService.getAllOrders(null, start, end);

        assertEquals(1, orders.size());
    }

    @Test
    void deleteOrder_shouldDeleteAndFreeTable() {
        DiningTable table = DiningTable.builder()
                .id(1L).tableNumber("T1").status(TableStatus.OCCUPIED)
                .currentOrderId(1L).capacity(4).tenantSchema("default").build();

        RestaurantOrder order = RestaurantOrder.builder()
                .id(1L).orderReference("ORD-001").status(OrderStatus.NEW)
                .tableId(1L).totalAmount(new BigDecimal("10.00"))
                .tenantSchema("default").createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(table));

        orderService.deleteOrder(1L);

        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        verify(orderRepository).delete(order);
    }
}
