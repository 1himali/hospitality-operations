package com.hospitality.operations.domain.room.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.hospitality.operations.domain.room.RoomStatus;
import com.hospitality.operations.domain.room.RoomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponseDto {

    private Long id;
    private String roomNumber;
    private RoomType type;
    private Integer floor;
    private RoomStatus status;
    private BigDecimal ratePerNight;
    private String imageUrl;
    private String issueNotes;
    private String tenantSchema;
    private Instant createdAt;
    private Instant updatedAt;
}
