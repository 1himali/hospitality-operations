package com.hospitality.operations.domain.inventory.dto;

import java.time.Instant;

import com.hospitality.operations.domain.inventory.InventoryStatus;
import com.hospitality.operations.domain.inventory.InventoryType;

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
public class InventoryItemResponseDto {

    private Long id;
    private String name;
    private InventoryType type;
    private String category;
    private Integer quantity;
    private Integer reorderLevel;
    private String unit;
    private InventoryStatus status;
    private String notes;
    private String tenantSchema;
    private Instant createdAt;
    private Instant updatedAt;
}
