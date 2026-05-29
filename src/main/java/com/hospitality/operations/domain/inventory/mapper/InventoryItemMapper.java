package com.hospitality.operations.domain.inventory.mapper;

import com.hospitality.operations.domain.inventory.InventoryItem;
import com.hospitality.operations.domain.inventory.InventoryStatus;
import com.hospitality.operations.domain.inventory.dto.InventoryItemRequestDto;
import com.hospitality.operations.domain.inventory.dto.InventoryItemResponseDto;

public final class InventoryItemMapper {

    private InventoryItemMapper() {
    }

    public static InventoryItem toEntity(InventoryItemRequestDto dto) {
        return InventoryItem.builder()
                .name(dto.getName())
                .type(dto.getType())
                .category(dto.getCategory())
                .quantity(dto.getQuantity() != null ? dto.getQuantity() : 0)
                .reorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : 5)
                .unit(dto.getUnit() != null ? dto.getUnit() : "pcs")
                .status(computeStatus(dto.getQuantity() != null ? dto.getQuantity() : 0,
                        dto.getReorderLevel() != null ? dto.getReorderLevel() : 5))
                .notes(dto.getNotes())
                .tenantSchema(dto.getTenantSchema() != null ? dto.getTenantSchema() : "default")
                .build();
    }

    public static InventoryItemResponseDto toDto(InventoryItem entity) {
        return InventoryItemResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .category(entity.getCategory())
                .quantity(entity.getQuantity())
                .reorderLevel(entity.getReorderLevel())
                .unit(entity.getUnit())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .tenantSchema(entity.getTenantSchema())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void updateEntity(InventoryItem entity, InventoryItemRequestDto dto) {
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setCategory(dto.getCategory());
        entity.setUnit(dto.getUnit() != null ? dto.getUnit() : "pcs");
        entity.setNotes(dto.getNotes());
        if (dto.getTenantSchema() != null) {
            entity.setTenantSchema(dto.getTenantSchema());
        }
        if (dto.getQuantity() != null) {
            entity.setQuantity(dto.getQuantity());
        }
        if (dto.getReorderLevel() != null) {
            entity.setReorderLevel(dto.getReorderLevel());
        }
        entity.setStatus(computeStatus(entity.getQuantity(), entity.getReorderLevel()));
    }

    public static InventoryStatus computeStatus(int quantity, int reorderLevel) {
        if (quantity <= 0) return InventoryStatus.OUT_OF_STOCK;
        if (quantity <= reorderLevel) return InventoryStatus.LOW_STOCK;
        return InventoryStatus.IN_STOCK;
    }
}
