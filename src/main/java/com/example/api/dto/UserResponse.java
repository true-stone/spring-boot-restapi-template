package com.example.api.dto;

import com.example.api.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * 회원 정보 응답 DTO
 */
@Builder
public record UserResponse(
        @Schema(
                description = "사용자 ID",
                example = "12345678-1234-1234-1234-123456789012",
                requiredMode = REQUIRED
        )
        String id,
        @Schema(
                description = "로그인 아이디",
                example = "testuser",
                requiredMode = REQUIRED
        )
        String username,
        @Schema(
                description = "사용자 이름",
                example = "테스트 유저",
                requiredMode = REQUIRED
        )
        String name,
        @Schema(
                description = "이메일 주소",
                example = "testuser@example.com",
                requiredMode = REQUIRED
        )
        String email,
        @Schema(
                description = "사용자 권한",
                example = "ROLE_USER",
                requiredMode = REQUIRED
        )
        String role,
        @Schema(
                description = "계정 생성 일시",
                example = "2023-07-20T12:34:56",
                requiredMode = REQUIRED
        )
        LocalDateTime createDate
) {
    public static UserResponse from(User user) {
        String userRoles = Optional.ofNullable(user.getRoles())
                .filter(roles -> !roles.isEmpty())
                .map(roles -> roles.stream()
                        .map(UserRole::authority)
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
