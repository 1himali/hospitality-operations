package com.hospitality.operations.domain.restaurant.menu.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.hospitality.operations.domain.restaurant.menu.MenuCategory;

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
public class MenuItemResponseDto {

    private Long id;
    private String name;
    private MenuCategory category;
    private BigDecimal price;
    private Boolean available;
    private String tenantSchema;
    private Instant createdAt;
    private Instant updatedAt;
}
