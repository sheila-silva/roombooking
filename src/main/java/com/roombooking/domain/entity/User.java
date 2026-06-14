package com.roombooking.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "user",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_email", columnNames = "email")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "bookings")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    // ─── Factory ────────────────────────────────────────────────────────────

    public static User create(String name, String email) {
        var user = new User();
        user.name      = name.strip();
        user.email     = email.strip().toLowerCase();
        user.createdAt = LocalDateTime.now();
        return user;
    }

    // ─── Mutators ───────────────────────────────────────────────────────────

    public void update(String name, String email) {
        this.name  = name.strip();
        this.email = email.strip().toLowerCase();
    }
}
