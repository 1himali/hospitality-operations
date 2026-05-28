package com.hospitality.operations.domain.restaurant.menu;

import java.util.List;

import com.hospitality.operations.domain.restaurant.menu.dto.MenuItemRequestDto;
import com.hospitality.operations.domain.restaurant.menu.dto.MenuItemResponseDto;

public interface MenuItemService {

    MenuItemResponseDto createMenuItem(MenuItemRequestDto requestDto);

    MenuItemResponseDto getMenuItemById(Long id);

    List<MenuItemResponseDto> getAllMenuItems();

    List<MenuItemResponseDto> getMenuItemsByTenantSchema(String tenantSchema);

    List<MenuItemResponseDto> getMenuItemsByCategory(MenuCategory category);

    List<MenuItemResponseDto> getMenuItemsByTenantSchemaAndCategory(String tenantSchema, MenuCategory category);

    MenuItemResponseDto updateMenuItem(Long id, MenuItemRequestDto requestDto);

    MenuItemResponseDto toggleAvailability(Long id);

    void deleteMenuItem(Long id);
}
