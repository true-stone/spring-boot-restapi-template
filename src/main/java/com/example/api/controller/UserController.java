package com.example.api.controller;

import com.example.api.annotation.CurrentUser;
import com.example.api.dto.UserCreateRequest;
import com.example.api.dto.UserResponse;
import com.example.api.entity.User;
import com.example.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 API
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> signUp(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        userService.signUp(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> myProfile(@CurrentUser User user) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(UserResponse.from(user));
    }
}
