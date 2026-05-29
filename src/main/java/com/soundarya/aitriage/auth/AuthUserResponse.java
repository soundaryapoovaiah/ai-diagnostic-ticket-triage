package com.soundarya.aitriage.auth;

import java.time.LocalDateTime;

public record AuthUserResponse(
        Long id,
        String fullName,
        String email,
        UserRole role,
        boolean enabled,
        LocalDateTime createdAt
) {
    public static AuthUserResponse fromEntity(AppUser user) {
        return new AuthUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}