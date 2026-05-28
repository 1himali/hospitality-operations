package com.hospitality.operations.domain.restaurant.menu;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.domain.restaurant.menu.dto.MenuItemRequestDto;
import com.hospitality.operations.domain.restaurant.menu.dto.MenuItemResponseDto;
import com.hospitality.operations.domain.restaurant.menu.mapper.MenuItemMapper;
import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;

    @Override
    @Transactional
    public MenuItemResponseDto createMenuItem(MenuItemRequestDto requestDto) {
        MenuItem item = MenuItemMapper.toEntity(requestDto);
        MenuItem saved = menuItemRepository.save(item);
        return MenuItemMapper.toDto(saved);
    }

    @Override
    public MenuItemResponseDto getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        return MenuItemMapper.toDto(item);
    }

    @Override
    public List<MenuItemResponseDto> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(MenuItemMapper::toDto)
                .toList();
    }

    @Override
    public List<MenuItemResponseDto> getMenuItemsByTenantSchema(String tenantSchema) {
        return menuItemRepository.findByTenantSchema(tenantSchema).stream()
                .map(MenuItemMapper::toDto)
                .toList();
    }

    @Override
    public List<MenuItemResponseDto> getMenuItemsByCategory(MenuCategory category) {
        return menuItemRepository.findByCategory(category).stream()
                .map(MenuItemMapper::toDto)
                .toList();
    }

    @Override
    public List<MenuItemResponseDto> getMenuItemsByTenantSchemaAndCategory(String tenantSchema, MenuCategory category) {
        return menuItemRepository.findByTenantSchemaAndCategory(tenantSchema, category).stream()
                .map(MenuItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public MenuItemResponseDto updateMenuItem(Long id, MenuItemRequestDto requestDto) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        MenuItemMapper.updateEntity(item, requestDto);
        MenuItem updated = menuItemRepository.save(item);
        return MenuItemMapper.toDto(updated);
    }

    @Override
    @Transactional
    public MenuItemResponseDto toggleAvailability(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        item.setAvailable(!item.getAvailable());
        MenuItem updated = menuItemRepository.save(item);
        return MenuItemMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        menuItemRepository.delete(item);
    }
}
