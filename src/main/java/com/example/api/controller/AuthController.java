package com.example.api.controller;

import com.example.api.annotation.ApiErrorCodeExample;
import com.example.api.annotation.CurrentUser;
import com.example.api.dto.LoginRequest;
import com.example.api.dto.LoginResponse;
import com.example.api.dto.RefreshRequest;
import com.example.api.entity.User;
import com.example.api.exception.ErrorCode;
import com.example.api.jwt.JwtProvider;
import com.example.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
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
@Tag(name = "인증 API", description = "사용자 로그인, 토큰 발급 등 인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "사용자 로그인", description = "아이디와 비밀번호를 사용하여 로그인하고, JWT 토큰을 발급받습니다.")
    @ApiErrorCodeExample({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_CREDENTIALS,
            ErrorCode.ACCOUNT_DISABLED,
            ErrorCode.ACCOUNT_LOCKED
    })
    @PostMapping("/login")
    public ResponseEntity<@NonNull LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        LoginResponse response = authService.login(loginRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtProvider.AUTHORIZATION_HEADER, JwtProvider.TOKEN_PREFIX + response.accessToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰과 리프레시 토큰을 발급합니다. (토큰 로테이션)")
    @ApiErrorCodeExample({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.REFRESH_TOKEN_NOT_FOUND,
            ErrorCode.TOKEN_EXPIRED
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.refresh(request.refreshToken());
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtProvider.AUTHORIZATION_HEADER, JwtProvider.TOKEN_PREFIX + response.accessToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화합니다.")
    @ApiErrorCodeExample({ErrorCode.INVALID_INPUT_VALUE})
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 기기 로그아웃", description = "해당 사용자의 모든 리프레시 토큰을 무효화합니다. 유효한 액세스 토큰이 필요합니다.")
    @ApiErrorCodeExample({ErrorCode.INVALID_TOKEN, ErrorCode.TOKEN_EXPIRED, ErrorCode.USER_NOT_FOUND})
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@Parameter(hidden = true) @CurrentUser User user) {
        authService.logoutAll(user.getPublicId());
        return ResponseEntity.noContent().build();
    }

}
