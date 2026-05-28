package com.hospitality.operations.domain.room;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByTenantSchema(String tenantSchema);

    List<Room> findByTenantSchemaAndStatus(String tenantSchema, RoomStatus status);

    List<Room> findByStatus(RoomStatus status);

    Optional<Room> findByIdAndTenantSchema(Long id, String tenantSchema);

    Optional<Room> findByRoomNumberAndTenantSchema(String roomNumber, String tenantSchema);

    boolean existsByRoomNumberAndTenantSchema(String roomNumber, String tenantSchema);
}
