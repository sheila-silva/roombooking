package com.roombooking.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BookingRequest {

    @NotNull(message = "Room id is required.")
    private Long roomId;

    @NotNull(message = "User id is required.")
    private Long userId;

    @NotNull(message = "Start time is required.")
    @Future(message = "Start time must be in the future.")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required.")
    private LocalDateTime endTime;
}
