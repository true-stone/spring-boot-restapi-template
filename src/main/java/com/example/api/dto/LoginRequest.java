package com.example.api.dto;

import com.example.api.annotation.ValidPassword;
import com.example.api.annotation.ValidUsername;

public record LoginRequest(
        @ValidUsername String username,
        @ValidPassword String password
) {
}