package com.example.api.dto;

import com.example.api.annotation.ValidPassword;
import com.example.api.annotation.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.example.api.constants.ValidationConstants.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * 로그인 요청 DTO
 */
@Schema(description = "로그인 요청 DTO")
public record LoginRequest(
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
        String password
) {
}
