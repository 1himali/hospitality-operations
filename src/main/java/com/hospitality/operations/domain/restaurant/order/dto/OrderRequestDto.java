package com.hospitality.operations.domain.restaurant.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
public class OrderRequestDto {

    private Long tableId;

    @Size(max = 20, message = "Order reference must not exceed 20 characters")
    private String orderReference;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemDto> items;

    @Size(max = 100, message = "Tenant schema must not exceed 100 characters")
    private String tenantSchema;
}
