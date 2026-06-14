package com.roombooking.repository;

import com.roombooking.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {



    Optional<Room> findByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
