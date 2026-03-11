package com.example.api.service;

import com.example.api.entity.RefreshToken;
import com.example.api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

// Redis 전환으로 비활성화. DB 기반 구현이 필요할 때 @Component 추가 후 사용.
// @Component
@RequiredArgsConstructor
public class JpaRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository repository;

    @Override
    @Transactional
    public void save(String token, Long userId, Instant expiresAt) {
        repository.save(RefreshToken.create(token, userId, expiresAt));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenInfo> findByToken(String token) {
        return repository.findByToken(token)
                .map(rt -> new TokenInfo(rt.getToken(), rt.getUserId(), rt.getExpiresAt()));
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        repository.deleteByToken(token);
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }
}
