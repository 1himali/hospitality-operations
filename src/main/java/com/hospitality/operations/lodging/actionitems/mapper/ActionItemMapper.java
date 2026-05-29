package com.hospitality.operations.lodging.actionitems.mapper;

import com.hospitality.operations.lodging.actionitems.ActionItem;
import com.hospitality.operations.lodging.actionitems.ActionItemStatus;
import com.hospitality.operations.lodging.actionitems.dto.ActionItemRequestDto;
import com.hospitality.operations.lodging.actionitems.dto.ActionItemResponseDto;

public final class ActionItemMapper {

    private ActionItemMapper() {
    }

    public static ActionItem toEntity(ActionItemRequestDto dto) {
        return ActionItem.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .status(ActionItemStatus.TODO)
                .tenantSchema(dto.getTenantSchema() != null ? dto.getTenantSchema() : "default")
                .build();
    }

    public static ActionItemResponseDto toDto(ActionItem entity) {
        return ActionItemResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .status(entity.getStatus())
                .tenantSchema(entity.getTenantSchema())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public static void updateEntity(ActionItem entity, ActionItemRequestDto dto) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
    }
}
