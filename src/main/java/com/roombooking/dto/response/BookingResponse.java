package com.roombooking.dto.response;

import com.roombooking.domain.entity.Booking;
import com.roombooking.domain.enums.BookingStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookingResponse {

    private final Long          id;
    private final Long          roomId;
    private final String        roomName;
    private final Long          userId;
    private final String        userName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final BookingStatus status;
    private final LocalDateTime createdAt;

    private BookingResponse(Booking booking) {
        this.id        = booking.getId();
        this.roomId    = booking.getRoom().getId();
        this.roomName  = booking.getRoom().getName();
        this.userId    = booking.getUser().getId();
        this.userName  = booking.getUser().getName();
        this.startTime = booking.getStartTime();
        this.endTime   = booking.getEndTime();
        this.status    = booking.getStatus();
        this.createdAt = booking.getCreatedAt();
    }

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking);
    }
}
