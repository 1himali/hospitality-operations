package com.hospitality.operations.domain.inventory;

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

import com.hospitality.operations.domain.activity.AuditHelper;
import com.hospitality.operations.domain.inventory.dto.InventoryItemRequestDto;
import com.hospitality.operations.domain.inventory.dto.InventoryItemResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryItemService inventoryService;
    private final AuditHelper auditHelper;

    @PostMapping
    public ResponseEntity<InventoryItemResponseDto> createInventoryItem(
            @Valid @RequestBody InventoryItemRequestDto requestDto) {
        InventoryItemResponseDto response = inventoryService.createInventoryItem(requestDto);
        auditHelper.record("CREATE", "INVENTORY_ITEM", response.getId(), "Created inventory item: " + response.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemResponseDto> getInventoryItemById(@PathVariable Long id) {
        InventoryItemResponseDto response = inventoryService.getInventoryItemById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponseDto>> getInventoryItems(
            @RequestParam(required = false) InventoryType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) InventoryStatus status) {
        List<InventoryItemResponseDto> items = inventoryService.getInventoryItems(type, category, status);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemResponseDto> updateInventoryItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemRequestDto requestDto) {
        InventoryItemResponseDto response = inventoryService.updateInventoryItem(id, requestDto);
        auditHelper.record("UPDATE", "INVENTORY_ITEM", id, "Updated inventory item: " + response.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<InventoryItemResponseDto> updateQuantity(
            @PathVariable Long id,
            @RequestParam int adjustment) {
        InventoryItemResponseDto prev = inventoryService.getInventoryItemById(id);
        InventoryItemResponseDto response = inventoryService.updateQuantity(id, adjustment);
        auditHelper.record("UPDATE", "INVENTORY_ITEM", id, "qty=" + prev.getQuantity(), "qty=" + response.getQuantity(), "Adjusted quantity by " + adjustment + " for: " + response.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InventoryItemResponseDto> setStatus(
            @PathVariable Long id,
            @RequestParam InventoryStatus status) {
        InventoryItemResponseDto prev = inventoryService.getInventoryItemById(id);
        InventoryItemResponseDto response = inventoryService.setStatus(id, status);
        auditHelper.record("STATUS_CHANGE", "INVENTORY_ITEM", id, "status=" + prev.getStatus(), "status=" + response.getStatus(), "Inventory status: " + prev.getStatus() + " \u2192 " + response.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        InventoryItemResponseDto prev = inventoryService.getInventoryItemById(id);
        inventoryService.deleteInventoryItem(id);
        auditHelper.record("DELETE", "INVENTORY_ITEM", id, "Deleted inventory item: " + prev.getName());
        return ResponseEntity.noContent().build();
    }
}
