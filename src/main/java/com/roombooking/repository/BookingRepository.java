package com.roombooking.repository;

import com.roombooking.domain.entity.Booking;
import com.roombooking.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByRoomId(Long roomId, Pageable pageable);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT b FROM Booking b
           WHERE b.room.id   = :roomId
             AND b.status    = 'ACTIVE'
             AND b.startTime < :endTime
             AND b.endTime   > :startTime
           """)
    List<Booking> findActiveOverlapping(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT b FROM Booking b
           WHERE b.room.id   = :roomId
             AND b.status    = 'ACTIVE'
             AND b.id       <> :excludeId
             AND b.startTime < :endTime
             AND b.endTime   > :startTime
           """)
    List<Booking> findActiveOverlappingExcluding(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("""
           SELECT b FROM Booking b
           WHERE b.room.id   = :roomId
             AND b.startTime >= :from
             AND b.endTime   <= :to
           ORDER BY b.startTime ASC
           """)
    Page<Booking> findByRoomAndPeriod(
            @Param("roomId") Long roomId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}