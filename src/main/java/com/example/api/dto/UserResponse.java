package com.example.api.dto;

import com.example.api.entity.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Builder
public record UserResponse(
        String id,
        String username,
        String name,
        String email,
        String role,
        LocalDateTime createDate
) {
    public static UserResponse from(User user) {
        String userRoles = Optional.ofNullable(user.getRoles())
                .filter(roles -> !roles.isEmpty())
                .map(roles -> roles.stream()
                        .map(UserRole::name)
                        .collect(Collectors.joining(",")))
                .orElse("");

        return UserResponse.builder()
                .id(user.getPublicId().toString())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(userRoles)
                .createDate(user.getCreateDate())
                .build();
    }
}
