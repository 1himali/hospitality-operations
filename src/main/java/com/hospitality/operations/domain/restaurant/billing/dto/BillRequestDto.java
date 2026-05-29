package com.hospitality.operations.domain.restaurant.billing.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class BillRequestDto {

    @NotNull(message = "Order ID is required")
    @Min(value = 1, message = "Order ID must be positive")
    private Long orderId;

    @Size(max = 100, message = "Server name must not exceed 100 characters")
    private String serverName;

    @Min(value = 0, message = "Discount must be non-negative")
    private BigDecimal discount;

    @Size(max = 100, message = "Tenant schema must not exceed 100 characters")
    private String tenantSchema;
}
