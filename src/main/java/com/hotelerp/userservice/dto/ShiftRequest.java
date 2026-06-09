package com.hotelerp.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequest {

    @NotBlank(message = "Shift name is required")
    private String shiftName;

    @NotBlank(message = "Shift code is required")
    private String shiftCode;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private Long hotelId;

    private String status;

    private String notes;
}
