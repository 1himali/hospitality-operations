package com.hospitality.operations.domain.restaurant.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.hospitality.operations.domain.restaurant.billing.BillStatus;

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
public class BillResponseDto {

    private Long id;
    private Long orderId;
    private String orderReference;
    private Long tableId;
    private String tableNumber;
    private String serverName;
    private String customerName;
    private String phoneNumber;
    private String email;
    private String invoiceNumber;
    private BillStatus status;
    private String notes;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal discount;
    private BigDecimal totalDue;
    private String aiDescription;
    private String tenantSchema;
    private Instant createdAt;

    private List<LineItemDto> lineItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItemDto {
        private Long id;
        private String itemType;
        private Long itemId;
        private Integer quantity;
        private String description;
        private BigDecimal unitPrice;
        private BigDecimal price;
    }
}
