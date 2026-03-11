package com.example.api.service;

import java.time.Instant;
import java.util.Optional;

/**
 * 리프레시 토큰 저장소 추상화 인터페이스.
 * 현재는 JPA(DB) 구현체를 사용하며, 추후 Redis 구현체로 교체 가능합니다.
 */
public interface RefreshTokenStore {

    record TokenInfo(String token, Long userId, Instant expiresAt) {
        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    void save(String token, Long userId, Instant expiresAt);

    Optional<TokenInfo> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);
}
