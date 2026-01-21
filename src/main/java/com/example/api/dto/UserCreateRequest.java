package com.example.api.dto;

import com.example.api.annotation.ValidPassword;
import com.example.api.annotation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @ValidUsername String username,
        @ValidPassword String password,
        @NotBlank String name,
        @Email @NotBlank String email
) {
}