package com.hospitality.operations.domain.restaurant.table.dto;

import java.time.Instant;

import com.hospitality.operations.domain.restaurant.table.TableStatus;
import com.hospitality.operations.domain.restaurant.table.TableType;

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
public class TableResponseDto {

    private Long id;
    private String tableNumber;
    private Integer capacity;
    private TableStatus status;
    private TableType tableType;
    private String location;
    private Instant seatedAt;
    private Long currentOrderId;
    private String reservationName;
    private Integer partySize;
    private Integer etaMinutes;
    private String tenantSchema;
    private Instant createdAt;
    private Instant updatedAt;
}
