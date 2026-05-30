package com.hospitality.operations.integration.thirdparty.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class ThirdPartyOrderResponseDto {

    private String orderId;
    private String listingId;
    private String listingName;
    private int quantity;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private String platform;
    private LocalDateTime createdAt;
}
