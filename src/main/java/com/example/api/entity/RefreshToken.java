package com.example.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    // DB 저장 시 조인 효율을 위해 내부 PK(Long) 사용
    // Redis 저장 시에는 publicId(UUID)를 사용 (RedisRefreshTokenStore 참고)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant expiresAt;

    public static RefreshToken create(String token, Long userId, Instant expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.token = token;
        rt.userId = userId;
        rt.expiresAt = expiresAt;
        return rt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
