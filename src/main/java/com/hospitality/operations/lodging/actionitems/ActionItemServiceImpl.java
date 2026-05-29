package com.hospitality.operations.lodging.actionitems;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.exception.ResourceNotFoundException;
import com.hospitality.operations.lodging.actionitems.dto.ActionItemRequestDto;
import com.hospitality.operations.lodging.actionitems.dto.ActionItemResponseDto;
import com.hospitality.operations.lodging.actionitems.mapper.ActionItemMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActionItemServiceImpl implements ActionItemService {

    private final ActionItemRepository actionItemRepository;

    @Override
    @Transactional
    public ActionItemResponseDto createActionItem(ActionItemRequestDto requestDto) {
        ActionItem entity = ActionItemMapper.toEntity(requestDto);
        ActionItem saved = actionItemRepository.save(entity);
        return ActionItemMapper.toDto(saved);
    }

    @Override
    public ActionItemResponseDto getActionItemById(Long id) {
        ActionItem entity = actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActionItem", "id", id));
        return ActionItemMapper.toDto(entity);
    }

    @Override
    public List<ActionItemResponseDto> getAllActionItems() {
        return actionItemRepository.findAll().stream()
                .map(ActionItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ActionItemResponseDto> getActionItemsByStatus(ActionItemStatus status) {
        return actionItemRepository.findByStatus(status).stream()
                .map(ActionItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ActionItemResponseDto> getActionItemsByCategory(ActionItemCategory category) {
        return actionItemRepository.findByCategory(category).stream()
                .map(ActionItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ActionItemResponseDto> getActionItemsByStatusAndCategory(ActionItemStatus status, ActionItemCategory category) {
        return actionItemRepository.findByStatusAndCategory(status, category).stream()
                .map(ActionItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ActionItemResponseDto updateActionItem(Long id, ActionItemRequestDto requestDto) {
        ActionItem entity = actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActionItem", "id", id));
        ActionItemMapper.updateEntity(entity, requestDto);
        ActionItem updated = actionItemRepository.save(entity);
        return ActionItemMapper.toDto(updated);
    }

    @Override
    @Transactional
    public ActionItemResponseDto updateActionItemStatus(Long id, ActionItemStatus status) {
        ActionItem entity = actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActionItem", "id", id));
        entity.setStatus(status);
        if (status == ActionItemStatus.COMPLETED) {
            entity.setCompletedAt(Instant.now());
        } else {
            entity.setCompletedAt(null);
        }
        ActionItem updated = actionItemRepository.save(entity);
        return ActionItemMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteActionItem(Long id) {
        ActionItem entity = actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActionItem", "id", id));
        actionItemRepository.delete(entity);
    }
}
