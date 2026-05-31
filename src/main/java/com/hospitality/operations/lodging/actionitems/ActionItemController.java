package com.hospitality.operations.lodging.actionitems;

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
import com.hospitality.operations.lodging.actionitems.dto.ActionItemRequestDto;
import com.hospitality.operations.lodging.actionitems.dto.ActionItemResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/action-items")
@RequiredArgsConstructor
public class ActionItemController {

    private final ActionItemService actionItemService;
    private final AuditHelper auditHelper;

    @PostMapping
    public ResponseEntity<ActionItemResponseDto> createActionItem(@Valid @RequestBody ActionItemRequestDto requestDto) {
        ActionItemResponseDto response = actionItemService.createActionItem(requestDto);
        auditHelper.record("CREATE", "ACTION_ITEM", response.getId(), "Created action item: " + response.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionItemResponseDto> getActionItemById(@PathVariable Long id) {
        ActionItemResponseDto response = actionItemService.getActionItemById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ActionItemResponseDto>> getActionItems(
            @RequestParam(required = false) ActionItemStatus status,
            @RequestParam(required = false) ActionItemCategory category) {

        List<ActionItemResponseDto> items;

        if (status != null && category != null) {
            items = actionItemService.getActionItemsByStatusAndCategory(status, category);
        } else if (status != null) {
            items = actionItemService.getActionItemsByStatus(status);
        } else if (category != null) {
            items = actionItemService.getActionItemsByCategory(category);
        } else {
            items = actionItemService.getAllActionItems();
        }

        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActionItemResponseDto> updateActionItem(@PathVariable Long id,
                                                                   @Valid @RequestBody ActionItemRequestDto requestDto) {
        ActionItemResponseDto response = actionItemService.updateActionItem(id, requestDto);
        auditHelper.record("UPDATE", "ACTION_ITEM", id, "Updated action item: " + response.getTitle());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ActionItemResponseDto> updateActionItemStatus(@PathVariable Long id,
                                                                        @RequestParam ActionItemStatus status) {
        ActionItemResponseDto prev = actionItemService.getActionItemById(id);
        ActionItemResponseDto response = actionItemService.updateActionItemStatus(id, status);
        auditHelper.record("STATUS_CHANGE", "ACTION_ITEM", id, "status=" + prev.getStatus(), "status=" + response.getStatus(), "Action item status: " + prev.getStatus() + " \u2192 " + response.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActionItem(@PathVariable Long id) {
        ActionItemResponseDto prev = actionItemService.getActionItemById(id);
        actionItemService.deleteActionItem(id);
        auditHelper.record("DELETE", "ACTION_ITEM", id, "Deleted action item: " + prev.getTitle());
        return ResponseEntity.noContent().build();
    }
}
