package com.example.api.dto;

import com.example.api.entity.User;
import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String username,
        String email
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
