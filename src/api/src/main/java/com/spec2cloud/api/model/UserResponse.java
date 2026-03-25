package com.spec2cloud.api.model;

import java.time.Instant;

public record UserResponse(String username, String role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.username(), user.role(), user.createdAt());
    }
}
