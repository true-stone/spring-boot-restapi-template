package com.example.api.service;

import com.example.api.entity.RefreshToken;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.RefreshTokenRepository;
import com.example.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

// Redis 전환으로 비활성화. DB 기반 구현이 필요할 때 @Component 추가 후 사용.
// @Component
@RequiredArgsConstructor
public class JpaRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void save(String token, UUID publicId, Instant expiresAt) {
        Long userId = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getId();
        repository.save(RefreshToken.create(token, userId, expiresAt));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenInfo> findByToken(String token) {
        return repository.findByToken(token)
                .map(rt -> {
                    UUID publicId = userRepository.findById(rt.getUserId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                            .getPublicId();
                    return new TokenInfo(token, publicId, rt.getExpiresAt());
                });
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        repository.deleteByToken(token);
    }

    @Override
    @Transactional
    public void deleteByPublicId(UUID publicId) {
        Long userId = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getId();
        repository.deleteByUserId(userId);
    }
}
