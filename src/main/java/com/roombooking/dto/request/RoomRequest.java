package com.roombooking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoomRequest {

    @NotBlank(message = "Room name must not be blank.")
    private String name;

    @NotNull(message = "Capacity is required.")
    @Min(value = 1, message = "Capacity must be at least 1.")
    private Integer capacity;

    /** Only used on updates; ignored on creation (always starts active). */
    private Boolean active;
}
