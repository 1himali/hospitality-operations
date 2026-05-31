package com.hospitality.operations.domain.restaurant.order;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.domain.activity.AuditHelper;
import com.hospitality.operations.domain.restaurant.order.dto.OrderRequestDto;
import com.hospitality.operations.domain.restaurant.order.dto.OrderResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class RestaurantOrderController {

    private final RestaurantOrderService orderService;
    private final AuditHelper auditHelper;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto requestDto) {
        OrderResponseDto response = orderService.createOrder(requestDto);
        auditHelper.record("CREATE", "ORDER", response.getId(), "Created order: " + response.getOrderReference());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderResponseDto response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo) {
        List<OrderResponseDto> orders = orderService.getAllOrders(status, dateFrom, dateTo);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(@PathVariable Long id,
                                                         @Valid @RequestBody OrderRequestDto requestDto) {
        OrderResponseDto response = orderService.updateOrder(id, requestDto);
        auditHelper.record("UPDATE", "ORDER", id, "Updated order: " + response.getOrderReference());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long id,
                                                               @RequestParam String status) {
        OrderResponseDto prev = orderService.getOrderById(id);
        OrderResponseDto response = orderService.updateOrderStatus(id, status);
        auditHelper.record("STATUS_CHANGE", "ORDER", id, "status=" + prev.getStatus(), "status=" + response.getStatus(), "Order status: " + prev.getStatus() + " \u2192 " + response.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        OrderResponseDto prev = orderService.getOrderById(id);
        orderService.deleteOrder(id);
        auditHelper.record("DELETE", "ORDER", id, "Deleted order: " + prev.getOrderReference());
        return ResponseEntity.noContent().build();
    }
}
