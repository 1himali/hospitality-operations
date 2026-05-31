package com.hospitality.operations.domain.room;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.domain.room.dto.RoomRequestDto;
import com.hospitality.operations.domain.room.dto.RoomResponseDto;
import com.hospitality.operations.domain.room.mapper.RoomMapper;
import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public RoomResponseDto createRoom(RoomRequestDto requestDto) {
        Room room = RoomMapper.toEntity(requestDto);
        Room savedRoom = roomRepository.save(room);
        return RoomMapper.toDto(savedRoom);
    }

    @Override
    public RoomResponseDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return RoomMapper.toDto(room);
    }

    @Override
    public List<RoomResponseDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomMapper::toDto)
                .toList();
    }

    @Override
    public List<RoomResponseDto> getRoomsByTenantSchema(String tenantSchema) {
        return roomRepository.findByTenantSchema(tenantSchema).stream()
                .map(RoomMapper::toDto)
                .toList();
    }

    @Override
    public List<RoomResponseDto> getRoomsByStatus(RoomStatus status) {
        return roomRepository.findByStatus(status).stream()
                .map(RoomMapper::toDto)
                .toList();
    }

    @Override
    public List<RoomResponseDto> getRoomsByTenantSchemaAndStatus(String tenantSchema, RoomStatus status) {
        return roomRepository.findByTenantSchemaAndStatus(tenantSchema, status).stream()
                .map(RoomMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RoomResponseDto updateRoom(Long id, RoomRequestDto requestDto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        RoomMapper.updateEntity(room, requestDto);
        Room updatedRoom = roomRepository.save(room);
        return RoomMapper.toDto(updatedRoom);
    }

    @Override
    @Transactional
    public RoomResponseDto updateRoomStatus(Long id, RoomStatus status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        room.setStatus(status);
        Room updatedRoom = roomRepository.save(room);
        return RoomMapper.toDto(updatedRoom);
    }

    @Override
    @Transactional
    public RoomResponseDto updateRoomNotes(Long id, String issueNotes) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        room.setIssueNotes(issueNotes);
        Room updatedRoom = roomRepository.save(room);
        return RoomMapper.toDto(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        roomRepository.delete(room);
    }
}
