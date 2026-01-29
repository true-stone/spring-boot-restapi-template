package com.example.api.dto;

import com.example.api.annotation.ValidPassword;
import com.example.api.annotation.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.example.api.constants.ValidationConstants.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * 회원가입 요청 DTO
 */
public record UserCreateRequest(
        @Schema(
                description = "로그인 아이디",
                example = "testuser",
                minLength = USERNAME_MIN_LENGTH,
                maxLength = USERNAME_MAX_LENGTH,
                pattern =  USERNAME_PATTERN,
                requiredMode = REQUIRED
        )
        @ValidUsername
        String username,

        @Schema(
                description = "비밀번호",
                example = "Password123!",
                minLength = PASSWORD_MIN_LENGTH,
                maxLength = PASSWORD_MAX_LENGTH,
                pattern =  PASSWORD_PATTERN,
                requiredMode = REQUIRED
        )
        @ValidPassword
        String password,

        @Schema(
                description = "이메일 주소",
                example = "testuser@example.com",
                requiredMode = REQUIRED
        )
        @Email
        @NotBlank
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