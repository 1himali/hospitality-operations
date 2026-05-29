package com.hospitality.operations.domain.restaurant.table;

import java.util.List;

import com.hospitality.operations.domain.restaurant.table.dto.TableRequestDto;
import com.hospitality.operations.domain.restaurant.table.dto.TableResponseDto;

public interface DiningTableService {

    TableResponseDto createTable(TableRequestDto requestDto);

    TableResponseDto getTableById(Long id);

    List<TableResponseDto> getAllTables();

    List<TableResponseDto> getTablesByStatus(TableStatus status);

    List<TableResponseDto> searchTables(String query, TableStatus status);

    TableResponseDto updateTable(Long id, TableRequestDto requestDto);

    TableResponseDto updateTableStatus(Long id, TableStatus status);

    void deleteTable(Long id);
}
