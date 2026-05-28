package com.hospitality.operations.domain.room;

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

import com.hospitality.operations.domain.room.dto.RoomRequestDto;
import com.hospitality.operations.domain.room.dto.RoomResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponseDto> createRoom(@Valid @RequestBody RoomRequestDto requestDto) {
        RoomResponseDto response = roomService.createRoom(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDto> getRoomById(@PathVariable Long id) {
        RoomResponseDto response = roomService.getRoomById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponseDto>> getRooms(
            @RequestParam(required = false) String tenantSchema,
            @RequestParam(required = false) RoomStatus status) {

        List<RoomResponseDto> rooms;

        if (tenantSchema != null && status != null) {
            rooms = roomService.getRoomsByTenantSchemaAndStatus(tenantSchema, status);
        } else if (tenantSchema != null) {
            rooms = roomService.getRoomsByTenantSchema(tenantSchema);
        } else if (status != null) {
            rooms = roomService.getRoomsByStatus(status);
        } else {
            rooms = roomService.getAllRooms();
        }

        return ResponseEntity.ok(rooms);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponseDto> updateRoom(@PathVariable Long id,
                                                       @Valid @RequestBody RoomRequestDto requestDto) {
        RoomResponseDto response = roomService.updateRoom(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RoomResponseDto> updateRoomStatus(@PathVariable Long id,
                                                             @RequestParam RoomStatus status) {
        RoomResponseDto response = roomService.updateRoomStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
