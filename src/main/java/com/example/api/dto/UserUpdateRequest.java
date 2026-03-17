package com.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record UserUpdateRequest(
        @Schema(
                description = "이메일",
                example = "username@abc.co.kr",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @Schema(
                description = "사용자 이름",
                example = "테스트 유저",
                requiredMode = REQUIRED
        )
        @NotBlank
        @Size(min = 2, max = 20)
        String name
) {
}
