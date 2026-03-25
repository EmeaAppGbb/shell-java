package com.spec2cloud.api.model;

import java.time.Instant;

public record User(
    String id,
    String username,
    String passwordHash,
    String role,
    Instant createdAt
) {
}
