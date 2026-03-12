package com.example.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String TOKEN_KEY_PREFIX = "refresh_token:";
    private static final String USER_TOKENS_KEY_PREFIX = "user_tokens:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String token, UUID publicId, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        String tokenKey = TOKEN_KEY_PREFIX + token;
        String userTokensKey = USER_TOKENS_KEY_PREFIX + publicId;

        redisTemplate.opsForValue().set(tokenKey, publicId.toString(), ttl);
        redisTemplate.opsForSet().add(userTokensKey, token);
        redisTemplate.expire(userTokensKey, ttl);
    }

    @Override
    public Optional<TokenInfo> findByToken(String token) {
        String publicId = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
        if (publicId == null) return Optional.empty();
        // Redis TTL이 만료를 관리하므로 값이 존재하면 유효한 토큰
        return Optional.of(new TokenInfo(token, UUID.fromString(publicId), Instant.MAX));
    }

    @Override
    public void deleteByToken(String token) {
        String publicId = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
        redisTemplate.delete(TOKEN_KEY_PREFIX + token);
        if (publicId != null) {
            redisTemplate.opsForSet().remove(USER_TOKENS_KEY_PREFIX + publicId, token);
        }
    }

    @Override
    public void deleteByPublicId(UUID publicId) {
        String userTokensKey = USER_TOKENS_KEY_PREFIX + publicId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
        if (tokens != null && !tokens.isEmpty()) {
            List<String> tokenKeys = tokens.stream()
                    .map(t -> TOKEN_KEY_PREFIX + t)
                    .toList();
            redisTemplate.delete(tokenKeys);
        }
        redisTemplate.delete(userTokensKey);
    }
}
