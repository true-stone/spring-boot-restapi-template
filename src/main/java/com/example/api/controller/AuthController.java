package com.example.api.controller;

import com.example.api.dto.LoginRequest;
import com.example.api.dto.LoginResponse;
import com.example.api.jwt.JwtProvider;
import com.example.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        LoginResponse response = authService.login(loginRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtProvider.AUTHORIZATION_HEADER, JwtProvider.TOKEN_PREFIX + response.accessToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        return  ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return  ResponseEntity.noContent().build();
    }

}