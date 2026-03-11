package com.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 갱신 요청 DTO")
public record RefreshRequest(
        @NotBlank
        @Schema(description = "리프레시 토큰", example = "550e8400-e29b-41d4", requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken
) {
}
