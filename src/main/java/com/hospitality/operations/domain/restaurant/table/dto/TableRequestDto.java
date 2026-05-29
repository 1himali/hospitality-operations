package com.hospitality.operations.domain.restaurant.table.dto;

import com.hospitality.operations.domain.restaurant.table.TableStatus;
import com.hospitality.operations.domain.restaurant.table.TableType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class TableRequestDto {

    @NotBlank(message = "Table number is required")
    @Size(max = 10, message = "Table number must not exceed 10 characters")
    private String tableNumber;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private TableStatus status;

    private TableType tableType;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    private Long currentOrderId;

    @Size(max = 100, message = "Reservation name must not exceed 100 characters")
    private String reservationName;

    @Min(value = 1, message = "Party size must be at least 1")
    private Integer partySize;

    @Min(value = 0, message = "ETA minutes must be non-negative")
    private Integer etaMinutes;

    @Size(max = 100, message = "Tenant schema must not exceed 100 characters")
    private String tenantSchema;
}
