package com.hospitality.operations.domain.inventory;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.domain.inventory.dto.InventoryItemRequestDto;
import com.hospitality.operations.domain.inventory.dto.InventoryItemResponseDto;
import com.hospitality.operations.domain.inventory.mapper.InventoryItemMapper;
import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository repository;

    @Override
    @Transactional
    public InventoryItemResponseDto createInventoryItem(InventoryItemRequestDto requestDto) {
        InventoryItem entity = InventoryItemMapper.toEntity(requestDto);
        InventoryItem saved = repository.save(entity);
        return InventoryItemMapper.toDto(saved);
    }

    @Override
    public InventoryItemResponseDto getInventoryItemById(Long id) {
        InventoryItem entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));
        return InventoryItemMapper.toDto(entity);
    }

    @Override
    public List<InventoryItemResponseDto> getAllInventoryItems() {
        return repository.findAll().stream()
                .map(InventoryItemMapper::toDto)
                .toList();
    }

    @Override
    public List<InventoryItemResponseDto> getInventoryItemsByType(InventoryType type) {
        return repository.findByType(type).stream()
                .map(InventoryItemMapper::toDto)
                .toList();
    }

    @Override
    public List<InventoryItemResponseDto> getInventoryItemsByCategory(String category) {
        return repository.findByCategory(category).stream()
                .map(InventoryItemMapper::toDto)
                .toList();
    }

    @Override
    public List<InventoryItemResponseDto> getInventoryItemsByStatus(InventoryStatus status) {
        return repository.findByStatus(status).stream()
                .map(InventoryItemMapper::toDto)
                .toList();
    }

    @Override
    public List<InventoryItemResponseDto> getInventoryItems(InventoryType type, String category, InventoryStatus status) {
        if (type != null && category != null && status != null) {
            return repository.findByTypeAndCategoryAndStatus(type, category, status).stream()
                    .map(InventoryItemMapper::toDto).toList();
        }
        if (type != null && category != null) {
            return repository.findByTypeAndCategory(type, category).stream()
                    .map(InventoryItemMapper::toDto).toList();
        }
        if (type != null && status != null) {
            return repository.findByTypeAndStatus(type, status).stream()
                    .map(InventoryItemMapper::toDto).toList();
        }
        if (type != null) {
            return getInventoryItemsByType(type);
        }
        if (category != null) {
            return getInventoryItemsByCategory(category);
        }
        if (status != null) {
            return getInventoryItemsByStatus(status);
        }
        return getAllInventoryItems();
    }

    @Override
    @Transactional
    public InventoryItemResponseDto updateInventoryItem(Long id, InventoryItemRequestDto requestDto) {
        InventoryItem entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));
        InventoryItemMapper.updateEntity(entity, requestDto);
        InventoryItem saved = repository.save(entity);
        return InventoryItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public InventoryItemResponseDto updateQuantity(Long id, int adjustment) {
        InventoryItem entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));
        int newQty = Math.max(0, entity.getQuantity() + adjustment);
        entity.setQuantity(newQty);
        entity.setStatus(InventoryItemMapper.computeStatus(newQty, entity.getReorderLevel()));
        InventoryItem saved = repository.save(entity);
        return InventoryItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public InventoryItemResponseDto setStatus(Long id, InventoryStatus status) {
        InventoryItem entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));
        entity.setStatus(status);
        InventoryItem saved = repository.save(entity);
        return InventoryItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteInventoryItem(Long id) {
        InventoryItem entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));
        repository.delete(entity);
    }
}
