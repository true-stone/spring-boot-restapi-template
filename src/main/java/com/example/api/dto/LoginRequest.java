package com.example.api.dto;

public record LoginRequest(
        String username
        , String password
) {
}