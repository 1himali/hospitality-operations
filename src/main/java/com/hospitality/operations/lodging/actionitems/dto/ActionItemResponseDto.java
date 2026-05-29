package com.hospitality.operations.lodging.actionitems.dto;

import java.time.Instant;

import com.hospitality.operations.lodging.actionitems.ActionItemCategory;
import com.hospitality.operations.lodging.actionitems.ActionItemStatus;

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
public class ActionItemResponseDto {

    private Long id;
    private String title;
    private String description;
    private ActionItemCategory category;
    private ActionItemStatus status;
    private String tenantSchema;
    private Instant createdAt;
    private Instant completedAt;
}
