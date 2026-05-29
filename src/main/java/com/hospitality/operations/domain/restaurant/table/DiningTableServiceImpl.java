package com.hospitality.operations.domain.restaurant.table;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.domain.restaurant.table.dto.TableRequestDto;
import com.hospitality.operations.domain.restaurant.table.dto.TableResponseDto;
import com.hospitality.operations.domain.restaurant.table.mapper.TableMapper;
import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiningTableServiceImpl implements DiningTableService {

    private final DiningTableRepository diningTableRepository;

    @Override
    @Transactional
    public TableResponseDto createTable(TableRequestDto requestDto) {
        DiningTable table = TableMapper.toEntity(requestDto);
        DiningTable saved = diningTableRepository.save(table);
        return TableMapper.toDto(saved);
    }

    @Override
    public TableResponseDto getTableById(Long id) {
        DiningTable table = diningTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", id));
        return TableMapper.toDto(table);
    }

    @Override
    public List<TableResponseDto> getAllTables() {
        return diningTableRepository.findAll().stream()
                .map(TableMapper::toDto)
                .toList();
    }

    @Override
    public List<TableResponseDto> getTablesByStatus(TableStatus status) {
        return diningTableRepository.findByStatus(status).stream()
                .map(TableMapper::toDto)
                .toList();
    }

    @Override
    public List<TableResponseDto> searchTables(String query, TableStatus status) {
        List<DiningTable> results;
        if (status != null) {
            results = diningTableRepository.searchByStatus(query, status);
        } else {
            results = diningTableRepository.search(query);
        }
        return results.stream()
                .map(TableMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TableResponseDto updateTable(Long id, TableRequestDto requestDto) {
        DiningTable table = diningTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", id));
        TableMapper.updateEntity(table, requestDto);
        DiningTable updated = diningTableRepository.save(table);
        return TableMapper.toDto(updated);
    }

    @Override
    @Transactional
    public TableResponseDto updateTableStatus(Long id, TableStatus status) {
        DiningTable table = diningTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", id));
        table.setStatus(status);

        // Auto-set seatedAt when table becomes occupied
        if (status == TableStatus.OCCUPIED && table.getSeatedAt() == null) {
            table.setSeatedAt(Instant.now());
        }
        // Clear transient fields when table becomes available
        if (status == TableStatus.AVAILABLE) {
            table.setSeatedAt(null);
            table.setCurrentOrderId(null);
            table.setReservationName(null);
            table.setPartySize(null);
            table.setEtaMinutes(null);
        }

        DiningTable updated = diningTableRepository.save(table);
        return TableMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteTable(Long id) {
        DiningTable table = diningTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", id));
        diningTableRepository.delete(table);
    }
}
