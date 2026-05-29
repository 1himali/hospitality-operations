package com.hospitality.operations.domain.restaurant.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long id;
    private String orderReference;
    private Long tableId;
    private String tableNumber;
    private String status;
    private BigDecimal totalAmount;
    private String tenantSchema;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemDto> items;
}
