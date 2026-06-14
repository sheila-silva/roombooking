package com.roombooking.service;

import com.roombooking.domain.entity.Room;
import com.roombooking.dto.request.RoomRequest;
import com.roombooking.dto.response.RoomResponse;
import com.roombooking.exception.BusinessException;
import com.roombooking.exception.ResourceNotFoundException;
import com.roombooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    // ── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<RoomResponse> findAll(Pageable pageable) {
        return roomRepository.findAll(pageable).map(RoomResponse::from);
    }

    @Transactional(readOnly = true)
    public RoomResponse findById(Long id) {
        return RoomResponse.from(fetchOrThrow(id));
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    @Transactional
    public RoomResponse create(RoomRequest request) {
        assertNameUnique(request.getName(), null);
        Room room = Room.create(request.getName(), request.getCapacity());
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        assertNameUnique(request.getName(), id);
        Room room   = fetchOrThrow(id);
        boolean active = request.getActive() != null ? request.getActive() : room.isActive();
        room.update(request.getName(), request.getCapacity(), active);
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        Room room = fetchOrThrow(id);
        room.deactivate();
        roomRepository.save(room);
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    public Room fetchOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    private void assertNameUnique(String name, Long excludeId) {
        boolean taken = excludeId == null
                ? roomRepository.findByName(name.strip()).isPresent()
                : roomRepository.existsByNameAndIdNot(name.strip(), excludeId);

        if (taken) {
            throw new BusinessException("name", "A room named '" + name + "' already exists.");
        }
    }
}
