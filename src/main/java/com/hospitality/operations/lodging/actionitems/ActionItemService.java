package com.hospitality.operations.lodging.actionitems;

import java.util.List;

import com.hospitality.operations.lodging.actionitems.dto.ActionItemRequestDto;
import com.hospitality.operations.lodging.actionitems.dto.ActionItemResponseDto;

public interface ActionItemService {

    ActionItemResponseDto createActionItem(ActionItemRequestDto requestDto);

    ActionItemResponseDto getActionItemById(Long id);

    List<ActionItemResponseDto> getAllActionItems();

    List<ActionItemResponseDto> getActionItemsByStatus(ActionItemStatus status);

    List<ActionItemResponseDto> getActionItemsByCategory(ActionItemCategory category);

    List<ActionItemResponseDto> getActionItemsByStatusAndCategory(ActionItemStatus status, ActionItemCategory category);

    ActionItemResponseDto updateActionItem(Long id, ActionItemRequestDto requestDto);

    ActionItemResponseDto updateActionItemStatus(Long id, ActionItemStatus status);

    void deleteActionItem(Long id);
}
