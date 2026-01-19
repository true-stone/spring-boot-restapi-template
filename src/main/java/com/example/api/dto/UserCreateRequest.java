package com.example.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Size(max = 20) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Email @NotBlank String email
) {
}
