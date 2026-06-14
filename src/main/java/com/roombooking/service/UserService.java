package com.roombooking.service;

import com.roombooking.domain.entity.User;
import com.roombooking.dto.request.UserRequest;
import com.roombooking.dto.response.UserResponse;
import com.roombooking.exception.BusinessException;
import com.roombooking.exception.ResourceNotFoundException;
import com.roombooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(fetchOrThrow(id));
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse create(UserRequest request) {
        assertEmailUnique(request.getEmail(), null);
        User user = User.create(request.getName(), request.getEmail());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        assertEmailUnique(request.getEmail(), id);
        User user = fetchOrThrow(id);
        user.update(request.getName(), request.getEmail());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = fetchOrThrow(id);
        userRepository.delete(user);
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    public User fetchOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void assertEmailUnique(String email, Long excludeId) {
        String normalised = email.strip().toLowerCase();
        boolean taken = excludeId == null
                ? userRepository.findByEmail(normalised).isPresent()
                : userRepository.existsByEmailAndIdNot(normalised, excludeId);

        if (taken) {
            throw new BusinessException("email", "Email '" + email + "' is already in use.");
        }
    }
}
