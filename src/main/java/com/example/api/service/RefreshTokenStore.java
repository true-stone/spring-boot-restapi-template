package com.example.api.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * 리프레시 토큰 저장소 추상화 인터페이스.
 * 현재는 Redis 구현체를 사용하며, DB 구현체(JpaRefreshTokenStore)로 교체 가능합니다.
 */
public interface RefreshTokenStore {

    record TokenInfo(String token, UUID publicId, Instant expiresAt) {
        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    void save(String token, UUID publicId, Instant expiresAt);

    Optional<TokenInfo> findByToken(String token);

    void deleteByToken(String token);

    void deleteByPublicId(UUID publicId);
}
