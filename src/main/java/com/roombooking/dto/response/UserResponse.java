package com.roombooking.dto.response;

import com.roombooking.domain.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponse {

    private final Long          id;
    private final String        name;
    private final String        email;
    private final LocalDateTime createdAt;

    private UserResponse(User user) {
        this.id        = user.getId();
        this.name      = user.getName();
        this.email     = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }

    public static UserResponse from(User user) {
        return new UserResponse(user);
    }
}
