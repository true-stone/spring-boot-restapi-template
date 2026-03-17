# 리프레시 토큰 저장소 전환 가이드

## 개요

리프레시 토큰 저장소는 `RefreshTokenStore` 인터페이스로 추상화되어 있어,
`AuthService` 코드 변경 없이 저장 백엔드를 교체할 수 있다.

```
AuthService
    └── RefreshTokenStore (interface)  ← UUID publicId 기반 공통 언어
            ├── JpaRefreshTokenStore   ← DB 방식 (비활성)
            └── RedisRefreshTokenStore ← Redis 방식 (현재 활성)
```

### 식별자 사용 전략

두 구현체는 사용자 식별자를 다르게 저장한다.

| 구현체 | 저장 식별자 | 이유 |
|--------|------------|------|
| `JpaRefreshTokenStore` | `Long userId` (내부 PK) | FK 조인 효율 유지 |
| `RedisRefreshTokenStore` | `UUID publicId` | 외부 식별자 일관성, Redis는 인덱스 불필요 |

인터페이스와 `AuthService`는 항상 `UUID publicId`로 통신하며,
`JpaRefreshTokenStore`가 내부적으로 `publicId ↔ Long id` 변환을 처리한다.

---

## Redis → DB 전환

### 1. 의존성 확인 (`build.gradle`)

`spring-boot-starter-data-redis`는 그대로 두거나 제거한다.
JPA는 이미 포함되어 있으므로 추가 작업 없다.

```groovy
// 제거해도 무방 (DB만 쓸 경우)
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

### 2. 프로필 yml에서 Redis 설정 제거

각 환경의 `application-{profile}.yml`에서 Redis 설정을 제거하거나 주석 처리한다.

```yaml
# spring:
#   data:
#     redis:
#       host: localhost
#       port: 6379
```

### 3. `JpaRefreshTokenStore.java` — `@Component` 활성화

```java
// 주석 해제
@Component
@RequiredArgsConstructor
public class JpaRefreshTokenStore implements RefreshTokenStore {
```

### 4. `RedisRefreshTokenStore.java` — `@Component` 비활성화

```java
// @Component  ← 주석 처리
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {
```

---

## DB → Redis 전환

### 1. 의존성 추가 (`build.gradle`)

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

### 2. 프로필 yml에 Redis 설정 추가

```yaml
spring:
  data:
    redis:
      host: localhost   # Redis 서버 주소
      port: 6379        # Redis 포트
```

### 3. `RedisRefreshTokenStore.java` — `@Component` 활성화

현재 파일에 `@Component`가 선언되어 있으므로 별도 작업 없다.

### 4. `JpaRefreshTokenStore.java` — `@Component` 비활성화

```java
// @Component  ← 주석 처리
@RequiredArgsConstructor
public class JpaRefreshTokenStore implements RefreshTokenStore {
```

---

## 저장 구조 비교

### Redis

```
refresh_token:{token}       →  publicId (UUID string)   TTL: refresh-token-expire-seconds
user_tokens:{publicId}      →  Set<token>               TTL: refresh-token-expire-seconds
```

- `refresh_token:{token}` : 토큰으로 publicId를 조회하는 기본 키
- `user_tokens:{publicId}` : 전체 기기 로그아웃(`POST /api/v1/auth/logout-all`) 시 사용

### DB

```
refresh_tokens 테이블
  token     VARCHAR(36)  UNIQUE  ← 조회 키
  user_id   BIGINT               ← users.id FK (Long, 조인 효율)
  expires_at TIMESTAMP
```

- publicId 대신 내부 PK(`Long id`)를 FK로 저장해 조인 성능 유지
- `JpaRefreshTokenStore`가 `UserRepository`를 통해 `publicId ↔ userId` 변환 처리

---

## 관련 파일

| 파일 | 역할 |
|------|------|
| `service/RefreshTokenStore.java` | 저장소 추상화 인터페이스 (`UUID publicId` 기반) |
| `service/JpaRefreshTokenStore.java` | DB(JPA) 구현체 — 내부적으로 `Long userId` 사용 |
| `service/RedisRefreshTokenStore.java` | Redis 구현체 — `UUID publicId` 직접 사용 |
| `entity/RefreshToken.java` | DB 방식 전용 JPA 엔티티 (`Long userId` 저장) |
| `repository/RefreshTokenRepository.java` | DB 방식 전용 JPA Repository |
| `service/AuthService.java` | `RefreshTokenStore`에만 의존 (변경 불필요) |
