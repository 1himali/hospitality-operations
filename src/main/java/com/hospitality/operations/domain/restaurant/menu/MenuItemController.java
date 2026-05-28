package com.hospitality.operations.domain.restaurant.menu;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.domain.restaurant.menu.dto.MenuItemRequestDto;
import com.hospitality.operations.domain.restaurant.menu.dto.MenuItemResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    public ResponseEntity<MenuItemResponseDto> createMenuItem(@Valid @RequestBody MenuItemRequestDto requestDto) {
        MenuItemResponseDto response = menuItemService.createMenuItem(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDto> getMenuItemById(@PathVariable Long id) {
        MenuItemResponseDto response = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponseDto>> getMenuItems(
            @RequestParam(required = false) String tenantSchema,
            @RequestParam(required = false) MenuCategory category) {

        List<MenuItemResponseDto> items;

        if (tenantSchema != null && category != null) {
            items = menuItemService.getMenuItemsByTenantSchemaAndCategory(tenantSchema, category);
        } else if (tenantSchema != null) {
            items = menuItemService.getMenuItemsByTenantSchema(tenantSchema);
        } else if (category != null) {
            items = menuItemService.getMenuItemsByCategory(category);
        } else {
            items = menuItemService.getAllMenuItems();
        }

        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDto> updateMenuItem(@PathVariable Long id,
                                                               @Valid @RequestBody MenuItemRequestDto requestDto) {
        MenuItemResponseDto response = menuItemService.updateMenuItem(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<MenuItemResponseDto> toggleAvailability(@PathVariable Long id) {
        MenuItemResponseDto response = menuItemService.toggleAvailability(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
