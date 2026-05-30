package com.hospitality.operations.integration.thirdparty.dto;

import java.math.BigDecimal;

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
public class ThirdPartyListingDto {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String platform;
}
