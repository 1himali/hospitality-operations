package com.hospitality.operations.domain.room;

import java.util.List;

import com.hospitality.operations.domain.room.dto.RoomRequestDto;
import com.hospitality.operations.domain.room.dto.RoomResponseDto;

public interface RoomService {

    RoomResponseDto createRoom(RoomRequestDto requestDto);

    RoomResponseDto getRoomById(Long id);

    List<RoomResponseDto> getAllRooms();

    List<RoomResponseDto> getRoomsByTenantSchema(String tenantSchema);

    List<RoomResponseDto> getRoomsByStatus(RoomStatus status);

    List<RoomResponseDto> getRoomsByTenantSchemaAndStatus(String tenantSchema, RoomStatus status);

    RoomResponseDto updateRoom(Long id, RoomRequestDto requestDto);

    RoomResponseDto updateRoomStatus(Long id, RoomStatus status);

    void deleteRoom(Long id);
}
