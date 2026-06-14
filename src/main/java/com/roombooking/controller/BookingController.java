package com.roombooking.controller;

import com.roombooking.dto.request.BookingRequest;
import com.roombooking.dto.response.BookingResponse;
import com.roombooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public Page<BookingResponse> list(
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        return bookingService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public BookingResponse findById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    /** Lists all bookings for a specific room (paginated). */
    @GetMapping("/room/{roomId}")
    public Page<BookingResponse> findByRoom(
            @PathVariable Long roomId,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        return bookingService.findByRoom(roomId, pageable);
    }

    /** Lists all bookings for a specific user (paginated). */
    @GetMapping("/user/{userId}")
    public Page<BookingResponse> findByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        return bookingService.findByUser(userId, pageable);
    }

    /**
     * Lists bookings for a room within a time window.
     * Example: GET /api/v1/bookings/room/1/period?from=2025-06-01T00:00&to=2025-06-30T23:59
     */
    @GetMapping("/room/{roomId}/period")
    public Page<BookingResponse> findByRoomAndPeriod(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        return bookingService.findByRoomAndPeriod(roomId, from, to, pageable);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Reschedules (updates) an existing active booking. */
    @PutMapping("/{id}")
    public BookingResponse reschedule(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request
    ) {
        return bookingService.reschedule(id, request);
    }

    /** Cancels a booking – status changes to CANCELLED; row is not deleted. */
    @DeleteMapping("/{id}")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
