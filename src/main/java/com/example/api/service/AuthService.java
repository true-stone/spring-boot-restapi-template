package com.example.api.service;

import com.example.api.dto.LoginRequest;
import com.example.api.dto.LoginResponse;
import com.example.api.entity.User;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ErrorCode;
import com.example.api.jwt.JwtProvider;
import com.example.api.repository.UserRepository;
import com.example.api.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.username(), request.password());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtProvider.generateAccessToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String refreshToken = issueRefreshToken(userDetails.user().getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                JwtProvider.TOKEN_PREFIX.trim(),
                jwtProvider.getAccessTokenExpireSeconds());
    }

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        RefreshTokenStore.TokenInfo stored = refreshTokenStore.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (stored.isExpired()) {
            refreshTokenStore.deleteByToken(refreshToken);
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(stored.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 토큰 로테이션: 기존 삭제 후 신규 발급
        refreshTokenStore.deleteByToken(refreshToken);
        String newRefreshToken = issueRefreshToken(user.getId());

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String newAccessToken = jwtProvider.generateAccessToken(auth);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                JwtProvider.TOKEN_PREFIX.trim(),
                jwtProvider.getAccessTokenExpireSeconds());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenStore.deleteByToken(refreshToken);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication result is null.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl userDetails)) {
            throw new IllegalStateException("Authentication principal is invalid.");
        }

        if (userDetails.user() == null || userDetails.user().getId() == null) {
            throw new IllegalStateException("Authenticated user id is missing.");
        }

        return userDetails.user().getId();
    }

    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenStore.deleteByUserId(userId);
    }

    private String issueRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(jwtProvider.getRefreshTokenExpireSeconds());
        refreshTokenStore.save(token, userId, expiresAt);
        return token;
    }
}
