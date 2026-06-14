package com.roombooking.service;

import com.roombooking.domain.entity.Booking;
import com.roombooking.domain.entity.Room;
import com.roombooking.domain.entity.User;
import com.roombooking.dto.request.BookingRequest;
import com.roombooking.dto.response.BookingResponse;
import com.roombooking.exception.ConflictException;
import com.roombooking.exception.ResourceNotFoundException;
import com.roombooking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomService       roomService;
    private final UserService       userService;

    // ── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<BookingResponse> findAll(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public BookingResponse findById(Long id) {
        return BookingResponse.from(fetchOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> findByRoom(Long roomId, Pageable pageable) {
        roomService.fetchOrThrow(roomId); // ensure room exists
        return bookingRepository.findByRoomId(roomId, pageable).map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> findByUser(Long userId, Pageable pageable) {
        userService.fetchOrThrow(userId); // ensure user exists
        return bookingRepository.findByUserId(userId, pageable).map(BookingResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> findByRoomAndPeriod(
            Long roomId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        roomService.fetchOrThrow(roomId);
        return bookingRepository.findByRoomAndPeriod(roomId, from, to, pageable)
                .map(BookingResponse::from);
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    /**
     * Creates a booking atomically:
     *   1. Acquires a pessimistic write lock on conflicting rows (via the repository query).
     *   2. Validates the room is active.
     *   3. Checks for overlapping ACTIVE bookings.
     *   4. Persists the new booking.
     *
     * The single @Transactional boundary guarantees that the read-for-conflict
     * and the subsequent write are executed as one atomic unit, preventing
     * simple race conditions under concurrent load.
     */
    @Transactional
    public BookingResponse create(BookingRequest request) {
        Room room = roomService.fetchOrThrow(request.getRoomId());
        User user = userService.fetchOrThrow(request.getUserId());

        assertNoOverlap(room.getId(), request.getStartTime(), request.getEndTime(), null);

        Booking booking = Booking.create(room, user, request.getStartTime(), request.getEndTime());
        return BookingResponse.from(bookingRepository.save(booking));
    }

    /**
     * Reschedules an existing ACTIVE booking.
     * Excludes the booking itself from the overlap check so it does not
     * conflict with its own previous slot.
     */
    @Transactional
    public BookingResponse reschedule(Long id, BookingRequest request) {
        Booking booking = fetchOrThrow(id);
        booking.assertActive();

        Room room = roomService.fetchOrThrow(request.getRoomId());
        User user = userService.fetchOrThrow(request.getUserId());

        assertNoOverlap(room.getId(), request.getStartTime(), request.getEndTime(), id);

        booking.reschedule(request.getStartTime(), request.getEndTime());
        return BookingResponse.from(bookingRepository.save(booking));
    }

    /**
     * Cancels a booking.  A cancelled booking is excluded from all future
     * conflict checks (see BookingRepository#findActiveOverlapping).
     */
    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = fetchOrThrow(id);
        booking.cancel();
        return BookingResponse.from(bookingRepository.save(booking));
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    public Booking fetchOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    /**
     * @param excludeId pass the booking's own id when rescheduling; null when creating.
     */
    private void assertNoOverlap(
            Long          roomId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long          excludeId
    ) {
        List<Booking> conflicts = excludeId == null
                ? bookingRepository.findActiveOverlapping(roomId, startTime, endTime)
                : bookingRepository.findActiveOverlappingExcluding(roomId, startTime, endTime, excludeId);

        if (!conflicts.isEmpty()) {
            Booking first = conflicts.getFirst();
            throw new ConflictException(
                    "Room already has an active booking from %s to %s (booking id: %d)."
                            .formatted(first.getStartTime(), first.getEndTime(), first.getId())
            );
        }
    }
}
