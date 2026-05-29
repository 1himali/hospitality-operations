package com.hospitality.operations.domain.inventory;

import java.util.List;

import com.hospitality.operations.domain.inventory.dto.InventoryItemRequestDto;
import com.hospitality.operations.domain.inventory.dto.InventoryItemResponseDto;

public interface InventoryItemService {

    InventoryItemResponseDto createInventoryItem(InventoryItemRequestDto requestDto);

    InventoryItemResponseDto getInventoryItemById(Long id);

    List<InventoryItemResponseDto> getAllInventoryItems();

    List<InventoryItemResponseDto> getInventoryItemsByType(InventoryType type);

    List<InventoryItemResponseDto> getInventoryItemsByCategory(String category);

    List<InventoryItemResponseDto> getInventoryItemsByStatus(InventoryStatus status);

    List<InventoryItemResponseDto> getInventoryItems(InventoryType type, String category, InventoryStatus status);

    InventoryItemResponseDto updateInventoryItem(Long id, InventoryItemRequestDto requestDto);

    InventoryItemResponseDto updateQuantity(Long id, int adjustment);

    InventoryItemResponseDto setStatus(Long id, InventoryStatus status);

    void deleteInventoryItem(Long id);
}
