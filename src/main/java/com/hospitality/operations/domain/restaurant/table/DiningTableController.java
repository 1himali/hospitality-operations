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

import com.hospitality.operations.domain.restaurant.table.dto.TableRequestDto;
import com.hospitality.operations.domain.restaurant.table.dto.TableResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService diningTableService;

    @PostMapping
    public ResponseEntity<TableResponseDto> createTable(@Valid @RequestBody TableRequestDto requestDto) {
        TableResponseDto response = diningTableService.createTable(requestDto);
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
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TableResponseDto> updateTableStatus(@PathVariable Long id,
                                                              @RequestParam TableStatus status) {
        TableResponseDto response = diningTableService.updateTableStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        diningTableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
