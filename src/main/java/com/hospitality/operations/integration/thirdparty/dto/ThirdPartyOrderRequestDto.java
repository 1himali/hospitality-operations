package com.hospitality.operations.integration.thirdparty.dto;

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
public class ThirdPartyOrderRequestDto {

    private String listingId;
    private int quantity;
    private String customerName;
    private String deliveryAddress;
    private String notes;
}
