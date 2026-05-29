package com.hospitality.operations.domain.inventory.dto;

import com.hospitality.operations.domain.inventory.InventoryType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class InventoryItemRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    @NotNull(message = "Type is required")
    private InventoryType type;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @Min(0)
    private Integer quantity;

    @Min(0)
    private Integer reorderLevel;

    @Size(max = 20)
    private String unit;

    @Size(max = 1000)
    private String notes;

    @Size(max = 100)
    private String tenantSchema;
}
