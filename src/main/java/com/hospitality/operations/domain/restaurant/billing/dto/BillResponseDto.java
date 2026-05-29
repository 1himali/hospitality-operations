package com.hospitality.operations.domain.restaurant.billing.dto;

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
public class BillResponseDto {

    private Long id;
    private Long orderId;
    private String orderReference;
    private Long tableId;
    private String serverName;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal discount;
    private BigDecimal totalDue;
    private String aiDescription;
    private String tenantSchema;
    private Instant createdAt;

    /** Line items from the order for the receipt view */
    private List<LineItemDto> lineItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItemDto {
        private Integer quantity;
        private String description;
        private BigDecimal unitPrice;
        private BigDecimal price;
    }
}
