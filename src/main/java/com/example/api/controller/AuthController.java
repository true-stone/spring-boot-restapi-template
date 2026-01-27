package com.example.api.controller;

import com.example.api.dto.ErrorResponse;
import com.example.api.dto.LoginRequest;
import com.example.api.dto.LoginResponse;
import com.example.api.jwt.JwtProvider;
import com.example.api.service.AuthService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (아이디/비밀번호 불일치, 계정 상태 비정상 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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

    @Hidden
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        return ResponseEntity.ok().build();
    }

    @Hidden
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.noContent().build();
    }

}
