package com.roombooking.dto.response;

import com.roombooking.domain.entity.Room;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RoomResponse {

    private final Long          id;
    private final String        name;
    private final int           capacity;
    private final boolean       active;
    private final LocalDateTime createdAt;

    private RoomResponse(Room room) {
        this.id        = room.getId();
        this.name      = room.getName();
        this.capacity  = room.getCapacity();
        this.active    = room.isActive();
        this.createdAt = room.getCreatedAt();
    }

    public static RoomResponse from(Room room) {
        return new RoomResponse(room);
    }
}
