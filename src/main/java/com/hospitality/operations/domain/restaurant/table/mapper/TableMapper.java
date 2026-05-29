package com.hospitality.operations.domain.restaurant.table.mapper;

import com.hospitality.operations.domain.restaurant.table.DiningTable;
import com.hospitality.operations.domain.restaurant.table.TableStatus;
import com.hospitality.operations.domain.restaurant.table.TableType;
import com.hospitality.operations.domain.restaurant.table.dto.TableRequestDto;
import com.hospitality.operations.domain.restaurant.table.dto.TableResponseDto;

public final class TableMapper {

    private TableMapper() {
        // utility class
    }

    public static DiningTable toEntity(TableRequestDto dto) {
        return DiningTable.builder()
                .tableNumber(dto.getTableNumber())
                .capacity(dto.getCapacity())
                .status(dto.getStatus() != null ? dto.getStatus() : TableStatus.AVAILABLE)
                .tableType(dto.getTableType() != null ? dto.getTableType() : TableType.DINING)
                .location(dto.getLocation())
                .currentOrderId(dto.getCurrentOrderId())
                .reservationName(dto.getReservationName())
                .partySize(dto.getPartySize())
                .etaMinutes(dto.getEtaMinutes())
                .tenantSchema(dto.getTenantSchema() != null ? dto.getTenantSchema() : "default")
                .build();
    }

    public static TableResponseDto toDto(DiningTable table) {
        return TableResponseDto.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .tableType(table.getTableType())
                .location(table.getLocation())
                .seatedAt(table.getSeatedAt())
                .currentOrderId(table.getCurrentOrderId())
                .reservationName(table.getReservationName())
                .partySize(table.getPartySize())
                .etaMinutes(table.getEtaMinutes())
                .tenantSchema(table.getTenantSchema())
                .createdAt(table.getCreatedAt())
                .updatedAt(table.getUpdatedAt())
                .build();
    }

    public static void updateEntity(DiningTable table, TableRequestDto dto) {
        table.setTableNumber(dto.getTableNumber());
        table.setCapacity(dto.getCapacity());
        if (dto.getStatus() != null) {
            table.setStatus(dto.getStatus());
        }
        if (dto.getTableType() != null) {
            table.setTableType(dto.getTableType());
        }
        table.setLocation(dto.getLocation());
        table.setCurrentOrderId(dto.getCurrentOrderId());
        table.setReservationName(dto.getReservationName());
        table.setPartySize(dto.getPartySize());
        table.setEtaMinutes(dto.getEtaMinutes());
        if (dto.getTenantSchema() != null) {
            table.setTenantSchema(dto.getTenantSchema());
        }
    }
}
