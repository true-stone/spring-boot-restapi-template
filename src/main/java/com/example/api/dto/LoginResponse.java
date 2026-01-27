package com.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * 로그인 성공 응답 DTO
 */
@Schema(description = "로그인 성공 응답 DTO")
public record LoginResponse(
        @Schema(
                description = "발급된 액세스 토큰 (JWT)",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlcjEiLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNj...",
                requiredMode = REQUIRED
        )
        String accessToken,

        @Schema(
                description = "토큰 타입",
                example = "Bearer",
                requiredMode = REQUIRED)
        String tokenType,

        @Schema(description = "액세스 토큰의 만료 시간 (초 단위)",
                example = "3600",
                requiredMode = REQUIRED
        )
        long expiresIn
) {
}
