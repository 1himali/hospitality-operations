package com.hospitality.operations.domain.restaurant.menu.mapper;

import com.hospitality.operations.domain.restaurant.menu.MenuItem;
import com.hospitality.operations.domain.restaurant.menu.dto.MenuItemRequestDto;
import com.hospitality.operations.domain.restaurant.menu.dto.MenuItemResponseDto;

public final class MenuItemMapper {

    private MenuItemMapper() {
        // utility class
    }

    public static MenuItem toEntity(MenuItemRequestDto dto) {
        return MenuItem.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .available(dto.getAvailable() != null ? dto.getAvailable() : true)
                .tenantSchema(dto.getTenantSchema() != null ? dto.getTenantSchema() : "default")
                .build();
    }

    public static MenuItemResponseDto toDto(MenuItem item) {
        return MenuItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .price(item.getPrice())
                .available(item.getAvailable())
                .tenantSchema(item.getTenantSchema())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public static void updateEntity(MenuItem item, MenuItemRequestDto dto) {
        item.setName(dto.getName());
        item.setCategory(dto.getCategory());
        item.setPrice(dto.getPrice());
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        if (dto.getTenantSchema() != null) {
            item.setTenantSchema(dto.getTenantSchema());
        }
    }
}
