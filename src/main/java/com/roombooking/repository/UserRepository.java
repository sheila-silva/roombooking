package com.roombooking.repository;

import com.roombooking.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAll(Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
