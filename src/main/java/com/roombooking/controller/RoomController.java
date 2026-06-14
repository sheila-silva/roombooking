package com.roombooking.controller;

import com.roombooking.dto.request.RoomRequest;
import com.roombooking.dto.response.RoomResponse;
import com.roombooking.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public Page<RoomResponse> list(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return roomService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public RoomResponse findById(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
        RoomResponse created = roomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public RoomResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request
    ) {
        return roomService.update(id, request);
    }

    /** Soft-deletes (deactivates) the room. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }
}
