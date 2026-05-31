package com.hospitality.operations.domain.restaurant.table;

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
import com.hospitality.operations.domain.restaurant.table.dto.TableRequestDto;
import com.hospitality.operations.domain.restaurant.table.dto.TableResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService diningTableService;
    private final AuditHelper auditHelper;

    @PostMapping
    public ResponseEntity<TableResponseDto> createTable(@Valid @RequestBody TableRequestDto requestDto) {
        TableResponseDto response = diningTableService.createTable(requestDto);
        auditHelper.record("CREATE", "DINING_TABLE", response.getId(), "Created table: " + response.getTableNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableResponseDto> getTableById(@PathVariable Long id) {
        TableResponseDto response = diningTableService.getTableById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TableResponseDto>> getTables(
            @RequestParam(required = false) TableStatus status,
            @RequestParam(required = false) String search) {

        List<TableResponseDto> tables;

        if (search != null && !search.isBlank()) {
            tables = diningTableService.searchTables(search.trim(), status);
        } else if (status != null) {
            tables = diningTableService.getTablesByStatus(status);
        } else {
            tables = diningTableService.getAllTables();
        }

        return ResponseEntity.ok(tables);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableResponseDto> updateTable(@PathVariable Long id,
                                                        @Valid @RequestBody TableRequestDto requestDto) {
        TableResponseDto response = diningTableService.updateTable(id, requestDto);
        auditHelper.record("UPDATE", "DINING_TABLE", id, "Updated table: " + response.getTableNumber());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TableResponseDto> updateTableStatus(@PathVariable Long id,
                                                               @RequestParam TableStatus status) {
        TableResponseDto prev = diningTableService.getTableById(id);
        TableResponseDto response = diningTableService.updateTableStatus(id, status);
        auditHelper.record("STATUS_CHANGE", "DINING_TABLE", id, "status=" + prev.getStatus(), "status=" + response.getStatus(), "Table status: " + prev.getStatus() + " \u2192 " + response.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        TableResponseDto prev = diningTableService.getTableById(id);
        diningTableService.deleteTable(id);
        auditHelper.record("DELETE", "DINING_TABLE", id, "Deleted table: " + prev.getTableNumber());
        return ResponseEntity.noContent().build();
    }
}
