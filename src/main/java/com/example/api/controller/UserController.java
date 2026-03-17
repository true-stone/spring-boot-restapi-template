package com.example.api.controller;

import com.example.api.annotation.ApiErrorCodeExample;
import com.example.api.dto.*;
import com.example.api.exception.ErrorCode;
import com.example.api.service.UserService;
import com.example.api.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 API
 */
@Tag(name = "사용자 API", description = "회원가입, 내 정보 조회 등 사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 시스템에 등록합니다. 이 API는 인증이 필요하지 않습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공")
    })
    @ApiErrorCodeExample({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.DUPLICATE_USERNAME,
            ErrorCode.DUPLICATE_EMAIL
    })
    @PostMapping
    public ResponseEntity<Void> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        userService.signUp(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 상세 정보를 조회합니다."
    )
    @ApiErrorCodeExample({
            ErrorCode.INVALID_TOKEN,
            ErrorCode.USER_NOT_FOUND
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> readMyProfile() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.readMyProfile(SecurityUtils.getUserPublicId()));
    }

    @Operation(
            summary = "내 정보 수정",
            description = "현재 로그인한 사용자의 이름과 이메일을 수정합니다."
    )
    @ApiErrorCodeExample({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.DUPLICATE_EMAIL
    })
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    @Operation(
            summary = "사용자 목록 조회 (관리자)",
            description = "전체 사용자 목록을 페이지네이션으로 조회합니다. ADMIN 권한이 필요합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @ApiErrorCodeExample({
            ErrorCode.INVALID_TOKEN,
            ErrorCode.ACCESS_DENIED
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> readUsers(
            @ParameterObject @Valid UserPageParam pageParam
    ) {
        return ResponseEntity.ok(userService.readUsers(pageParam));
    }
}
