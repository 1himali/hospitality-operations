package com.hospitality.operations.domain.room.dto;

import java.math.BigDecimal;

import com.hospitality.operations.domain.room.RoomStatus;
import com.hospitality.operations.domain.room.RoomType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
public class RoomRequestDto {

    @NotBlank(message = "Room number is required")
    @Size(max = 10, message = "Room number must not exceed 10 characters")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private RoomType type;

    @NotNull(message = "Floor is required")
    @Min(value = 0, message = "Floor must be 0 or greater")
    private Integer floor;

    private RoomStatus status;

    @NotNull(message = "Rate per night is required")
    @DecimalMin(value = "0.01", message = "Rate per night must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Rate per night must have at most 8 integer digits and 2 decimal places")
    private BigDecimal ratePerNight;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 500, message = "Issue notes must not exceed 500 characters")
    private String issueNotes;

    @Size(max = 100, message = "Tenant schema must not exceed 100 characters")
    private String tenantSchema;
}
