package com.hospitality.operations.domain.restaurant.billing.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @Min(value = 1, message = "Order ID must be positive")
    private Long orderId;

    private List<RoomBillItem> roomItems;

    private List<AdditionalItemDto> additionalItems;

    @Size(max = 100, message = "Server name must not exceed 100 characters")
    private String serverName;

    @Min(value = 0, message = "Discount must be non-negative")
    private BigDecimal discount;

    @Size(max = 100, message = "Tenant schema must not exceed 100 characters")
    private String tenantSchema;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomBillItem {

        @NotNull(message = "Room ID is required")
        private Long roomId;

        @NotNull(message = "Nights is required")
        @Min(value = 1, message = "Nights must be at least 1")
        private Integer nights;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdditionalItemDto {

        @NotBlank(message = "Description is required")
        @Size(max = 200, message = "Description must not exceed 200 characters")
        private String description;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        private BigDecimal unitPrice;
    }
}
