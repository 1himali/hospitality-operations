package com.hospitality.operations.domain.restaurant.billing.dto;

import java.math.BigDecimal;
import java.util.List;

import com.hospitality.operations.domain.restaurant.billing.BillStatus;

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

    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    private String customerName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 200, message = "Email must not exceed 200 characters")
    private String email;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @Min(value = 0, message = "Discount must be non-negative")
    private BigDecimal discount;

    private BillStatus status;

    private List<Long> selectedRoomIds;

    private List<Long> selectedTableIds;

    private List<Long> selectedMenuItemIds;

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
