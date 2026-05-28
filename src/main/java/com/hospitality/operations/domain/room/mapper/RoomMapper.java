package com.hospitality.operations.domain.room.mapper;

import com.hospitality.operations.domain.room.Room;
import com.hospitality.operations.domain.room.RoomStatus;
import com.hospitality.operations.domain.room.dto.RoomRequestDto;
import com.hospitality.operations.domain.room.dto.RoomResponseDto;

public final class RoomMapper {

    private RoomMapper() {
        // utility class
    }

    public static Room toEntity(RoomRequestDto dto) {
        return Room.builder()
                .roomNumber(dto.getRoomNumber())
                .type(dto.getType())
                .floor(dto.getFloor())
                .status(dto.getStatus() != null ? dto.getStatus() : RoomStatus.VACANT)
                .ratePerNight(dto.getRatePerNight())
                .imageUrl(dto.getImageUrl())
                .issueNotes(dto.getIssueNotes())
                .tenantSchema(dto.getTenantSchema() != null ? dto.getTenantSchema() : "default")
                .build();
    }

    public static RoomResponseDto toDto(Room room) {
        return RoomResponseDto.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .type(room.getType())
                .floor(room.getFloor())
                .status(room.getStatus())
                .ratePerNight(room.getRatePerNight())
                .imageUrl(room.getImageUrl())
                .issueNotes(room.getIssueNotes())
                .tenantSchema(room.getTenantSchema())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    public static void updateEntity(Room room, RoomRequestDto dto) {
        room.setRoomNumber(dto.getRoomNumber());
        room.setType(dto.getType());
        room.setFloor(dto.getFloor());
        if (dto.getStatus() != null) {
            room.setStatus(dto.getStatus());
        }
        room.setRatePerNight(dto.getRatePerNight());
        room.setImageUrl(dto.getImageUrl());
        room.setIssueNotes(dto.getIssueNotes());
        if (dto.getTenantSchema() != null) {
            room.setTenantSchema(dto.getTenantSchema());
        }
    }
}
