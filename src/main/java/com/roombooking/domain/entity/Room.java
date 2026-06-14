package com.roombooking.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "room",
        uniqueConstraints = @UniqueConstraint(name = "uq_room_name", columnNames = "name")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "bookings")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    // ─── Factory ────────────────────────────────────────────────────────────

    public static Room create(String name, int capacity) {
        assertPositiveCapacity(capacity);
        var room = new Room();
        room.name      = name.strip();
        room.capacity  = capacity;
        room.active    = true;
        room.createdAt = LocalDateTime.now();
        return room;
    }

    // ─── Mutators ───────────────────────────────────────────────────────────

    public void update(String name, int capacity, boolean active) {
        assertPositiveCapacity(capacity);
        this.name     = name.strip();
        this.capacity = capacity;
        this.active   = active;
    }

    public void deactivate() {
        this.active = false;
    }

    // ─── Domain assertions ──────────────────────────────────────────────────

    public void assertActive() {
        if (!active) {
            throw new com.roombooking.exception.BusinessException(
                    "roomId", "Room '" + name + "' is inactive and cannot accept bookings."
            );
        }
    }

    private static void assertPositiveCapacity(int capacity) {
        if (capacity <= 0) {
            throw new com.roombooking.exception.BusinessException(
                    "capacity", "Capacity must be a positive value, but got: " + capacity
            );
        }
    }
}
