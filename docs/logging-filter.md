# HTTP 요청/응답 로깅 필터

## 개요

모든 HTTP 요청과 응답을 **1줄 구조화 로그**로 기록하는 필터다.
단순한 액세스 로그를 넘어 에러·슬로우 요청에서는 헤더와 body 를 조건부로 확장하되,
민감 정보 노출과 로그 폭주를 방지하는 것이 핵심 설계 목표다.

---

## 클래스 구조

```
LoggingFilter                  ← Spring Filter, 진입점
    │
    ├─ HttpLogPolicy            ← "무엇을 로그할지" 결정
    │       │
    │       └─▶ HttpLogContext  ← 결정 결과를 담는 데이터 컨테이너
    │
    └─ HttpLogFormatter         ← "어떻게 포맷할지" 처리
```

| 클래스 | 책임 |
|---|---|
| `LoggingFilter` | 필터 체인 실행, 래퍼 적용, 로그 레벨 분기 |
| `HttpLogPolicy` | 상태 코드·latency·예외를 분석해 headers/body 포함 여부와 로그 레벨 결정 |
| `HttpLogContext` | Policy 결정 결과와 Formatter 출력값을 함께 보관하는 컨테이너 |
| `HttpLogFormatter` | 헤더·body 추출, 마스킹, compact 처리, 최종 로그 문자열 조립 |

---

## 요청 처리 흐름

```
HTTP 요청
    │
    ▼
LoggingFilter.doFilterInternal()
    │
    ├─ 1. START_TIME 기록 (request attribute)
    ├─ 2. ContentCachingRequestWrapper  ┐ body를 소비해도
    │     ContentCachingResponseWrapper ┘ 원본 스트림 유지
    │
    ├─ 3. filterChain.doFilter() 실행
    │        (예외 발생 시 thrown 에 포획 후 재던짐)
    │
    └─ 4. finally 블록
            │
            ├─ [Async 대기 중] → copyBodyToResponse() 후 return (로깅 스킵)
            ├─ [이미 로깅됨]  → copyBodyToResponse() 후 return (중복 방지)
            │
            ├─ 5. HttpLogPolicy.decide()   → HttpLogContext 생성
            ├─ 6. HttpLogFormatter.format() → 1줄 로그 문자열 조립
            ├─ 7. 레벨에 따라 log.info/warn/error() 호출
            └─ 8. copyBodyToResponse()    (클라이언트로 응답 전달)
```

---

## Async 요청 처리

`Callable`, `DeferredResult` 등 비동기 응답은 Servlet 디스패치가 2회 발생한다.

| 디스패치 | `isAsyncStarted()` | `isAsyncDispatch()` | 로깅 여부 |
|---|---|---|---|
| REQUEST (비동기 시작) | `true` | `false` | 스킵 |
| ASYNC (비동기 완료) | `false` | `true` | **실행** |

`shouldNotFilterAsyncDispatch()` 를 `false` 로 오버라이드해 ASYNC 디스패치에서도 필터가 실행되도록 한다.
로깅은 ASYNC 완료 시점에 1회만 수행되므로 전체 latency(비동기 대기 시간 포함)가 정확하게 측정된다.

---

## 로그 출력 형식

```
[HTTP] {METHOD} {URI} -> {STATUS} ({DURATION}ms) ts={ISO-8601}
      [slow=true]
      [ex={ExceptionSimpleName}]
      [headers={...}]
      [req={...}]
      [res={...}]
```

### 예시

**정상 요청 (INFO)**
```
[HTTP] GET /api/v1/users/me -> 200 (12ms) ts=2025-03-03T10:22:11+09:00
```

**슬로우 요청 + 헤더 포함 (WARN)**
```
[HTTP] GET /api/v1/items -> 200 (2345ms) ts=2025-03-03T10:22:11+09:00 slow=true
       headers={content-type=application/json, user-agent=Mozilla/5.0...}
```

**5xx 에러 + body 포함 (ERROR)**
```
[HTTP] POST /api/v1/orders -> 500 (88ms) ts=2025-03-03T10:22:11+09:00
       ex=NullPointerException
       headers={content-type=application/json, host=localhost:8080}
       req={"productId":42,"quantity":1}
       res={"code":"C002","message":"서버 내부 오류가 발생했습니다."}
```

**body 가 inline 한도 초과 시 compact 형태**
```
[HTTP] POST /api/v1/bulk -> 500 (312ms) ts=...
       res={len=5120, preview={"items":[{"id":1,...(truncated), gzip64=H4sIAAAAA...}
```

---

## Policy — 로그 레벨 및 포함 여부 결정

### 로그 레벨

| 조건 | 레벨 |
|---|---|
| 5xx 또는 예외 발생 | `ERROR` |
| 4xx 또는 슬로우 요청 | `WARN` |
| 그 외 | `INFO` |

### 헤더 포함 여부

| 조건 | 헤더 출력 |
|---|---|
| 2xx / 3xx 정상 응답 | 제외 |
| 4xx / 5xx / 예외 / 슬로우 | 포함 |
| **401 / 403 / 404** | **강제 제외** (민감 정보·노이즈 방지) |

### Body 포함 여부

| 조건 | Body 출력 |
|---|---|
| DEBUG 레벨 활성화 | 항상 포함 |
| 5xx 또는 예외 발생 | 포함 |
| **401 / 403 / 404** | **강제 제외** |
| 그 외 (1xx/2xx/3xx/4xx) | 제외 |

> Body 로깅은 JSON / text 계열 Content-Type 에만 적용된다.
> `multipart/form-data`, `application/octet-stream` 등 바이너리 타입은 로깅하지 않는다.

---

## 보안 — 민감 정보 마스킹

### 민감 헤더 (`SENSITIVE_HEADERS`)

아래 헤더는 값이 `***` 로 치환된다.

```
authorization, cookie, set-cookie, proxy-authorization
```

### 허용 헤더 (`ALLOWED_HEADERS`)

아래 목록에 포함된 헤더만 출력된다 (allowlist 방식).

```
content-type, accept, user-agent, x-request-id,
x-forwarded-for, x-real-ip, referer, host
```

### 민감 JSON 키 (`SENSITIVE_JSON_KEYS`)

Request / Response body 의 JSON 에서 아래 키의 값은 `***` 로 치환된다.
Object · Array 를 재귀적으로 순회하므로 중첩 구조에도 적용된다.

```
password, accesstoken
```

> **주의:** gzip64 는 압축일 뿐 비식별화가 아니다(복원 가능).
> 로그 저장소의 접근 통제와 보관 기간 정책을 반드시 함께 관리해야 한다.

---

## Compact 전략 — 긴 헤더/Body 처리

로그 한 줄이 과도하게 길어지는 것을 방지하기 위해 인라인 한도 초과 시 축약한다.

```
{len=<원본 길이>, preview=<앞부분 256자>, gzip64=<GZIP+Base64>}
```

gzip64 결과가 `MAX_GZIP64_LENGTH(8,192자)` 를 초과하면 `gzip64=omitted` 로 생략한다.
preview 만으로도 빠른 육안 확인이 가능하고, gzip64 는 외부 도구로 원문 복원이 가능하다.

```bash
# gzip64 복원 예시
echo "<gzip64_string>" | base64 -d | gunzip
```

---

## 설정 상수 (`LoggingFilter`)

| 상수 | 기본값 | 설명 |
|---|---|---|
| `REQUEST_CACHE_LIMIT` | 10,240 bytes | 요청 body 캐시 최대 크기 |
| `LOG_HEADERS` | `true` | 헤더 로깅 활성화 여부 |
| `LOG_BODY_ON_ERROR_ONLY` | `true` | 에러 시에만 body 로깅 |
| `LOG_BODY_ON_5XX` | `true` | 5xx 에서 body 로깅 |
| `SLOW_THRESHOLD_MS` | 2,000 ms | 슬로우 요청 판단 기준 |
| `PREVIEW_LEN` | 256 chars | compact preview 길이 |
| `MAX_HEADERS_INLINE_LENGTH` | 512 chars | 헤더 인라인 최대 길이 |
| `MAX_BODY_INLINE_LENGTH` | 2,048 chars | body 인라인 최대 길이 |
| `MAX_GZIP64_LENGTH` | 8,192 chars | gzip64 최대 출력 길이 |
| `MAX_HEADER_VALUE_LENGTH` | 256 chars | 헤더 개별 값 최대 길이 |

---

## 커스터마이징 포인트

### 민감 키 추가

`LoggingFilter` 의 `SENSITIVE_JSON_KEYS` 에 키 이름을 추가한다.

```java
private static final Set<String> SENSITIVE_JSON_KEYS = Set.of(
        "password", "accesstoken", "refreshtoken", "ssn"  // 추가
);
```

### 허용 헤더 추가

`ALLOWED_HEADERS` 에 헤더 이름을 추가한다.

```java
private static final List<String> ALLOWED_HEADERS = List.of(
        "content-type", "accept", "user-agent",
        "x-request-id", "x-correlation-id"  // 추가
);
```

### 슬로우 임계값 조정

```java
private static final long SLOW_THRESHOLD_MS = 1_000;  // 1초로 변경
```

### Body 로깅 비활성화

5xx 에서도 body 를 남기지 않으려면:

```java
private static final boolean LOG_BODY_ON_5XX = false;
```

---

## 관련 클래스

| 경로 | 설명 |
|---|---|
| `filter/LoggingFilter.java` | 필터 진입점 |
| `filter/logging/HttpLogPolicy.java` | 로그 포함 여부·레벨 결정 |
| `filter/logging/HttpLogContext.java` | 결정 결과 및 포맷 결과 컨테이너 |
| `filter/logging/HttpLogFormatter.java` | 헤더·body 포맷, 마스킹, compact |
| `util/CompressUtils.java` | gzip + Base64 인코딩 유틸 |
| `config/SecurityFilterRegistrationConfig.java` | 필터 등록 설정 |