package com.roombooking.domain.entity;

import com.roombooking.domain.enums.BookingStatus;
import com.roombooking.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── Factory ────────────────────────────────────────────────────────────

    public static Booking create(Room room, User user, LocalDateTime startTime, LocalDateTime endTime) {
        assertDatesValid(startTime, endTime);
        room.assertActive();

        var booking = new Booking();
        booking.room      = room;
        booking.user      = user;
        booking.startTime = startTime;
        booking.endTime   = endTime;
        booking.status    = BookingStatus.ACTIVE;
        booking.createdAt = LocalDateTime.now();
        return booking;
    }

    // ─── Mutators ───────────────────────────────────────────────────────────

    public void reschedule(LocalDateTime startTime, LocalDateTime endTime) {
        assertActive();
        assertDatesValid(startTime, endTime);
        this.startTime = startTime;
        this.endTime   = endTime;
    }

    public void cancel() {
        assertActive();
        this.status = BookingStatus.CANCELLED;
    }

    // ─── Domain assertions ──────────────────────────────────────────────────

    public void assertActive() {
        if (!status.isActive()) {
            throw new BusinessException(
                    "status", "Booking #" + id + " is already cancelled and cannot be modified."
            );
        }
    }

    private static void assertDatesValid(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("startTime", "Start and end times must not be null.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(
                    "endTime", "End time must be strictly after start time."
            );
        }
    }
}
