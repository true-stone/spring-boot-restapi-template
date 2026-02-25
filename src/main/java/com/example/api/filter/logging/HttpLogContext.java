package com.example.api.filter.logging;

import lombok.Getter;

/**
 * 한 HTTP 요청/응답에 대한 로깅 컨텍스트.
 *
 * <p>{@link HttpLogPolicy}가 생성하고, {@link HttpLogFormatter}가 소비한다.
 * 불변 필드(요청 메타·플래그)는 생성자에서 확정되며,
 * 가변 필드(headers/body 포맷 결과)는 Formatter가 채운다.</p>
 */
@Getter
public class HttpLogContext {

    // ---- 요청 기본 정보 ----
    private final String method;
    private final String uri;
    private final int status;

    // ---- 타이밍 ----
    private final long startMillis;
    private final long durationMs;
    /** 로그 라인의 ts= 필드에 출력할 ISO-8601 문자열 */
    private final String startedAtIso;

    // ---- 상태 플래그 ----
    private final boolean is4xx;
    private final boolean is5xx;
    private final boolean isException;
    /** is4xx || is5xx || isException */
    private final boolean isError;
    private final boolean isSlow;

    private final Throwable thrown;

    // ---- Policy 결정 결과 ----
    /** true 이면 Formatter 가 헤더를 포맷해 headersValue 에 채운다 */
    private final boolean includeHeaders;
    /** true 이면 Formatter 가 req/res body 를 포맷해 채운다 */
    private final boolean includeBody;

    // ---- Formatter 가 채우는 출력 값 ----
    private String headersValue;
    private String reqBodyValue;
    private String resBodyValue;

    public HttpLogContext(String method, String uri, int status,
                         long startMillis, long durationMs, String startedAtIso,
                         boolean is4xx, boolean is5xx, boolean isException,
                         boolean isError, boolean isSlow, Throwable thrown,
                         boolean includeHeaders, boolean includeBody) {
        this.method = method;
        this.uri = uri;
        this.status = status;
        this.startMillis = startMillis;
        this.durationMs = durationMs;
        this.startedAtIso = startedAtIso;
        this.is4xx = is4xx;
        this.is5xx = is5xx;
        this.isException = isException;
        this.isError = isError;
        this.isSlow = isSlow;
        this.thrown = thrown;
        this.includeHeaders = includeHeaders;
        this.includeBody = includeBody;
    }

    // includeHeaders/includeBody 는 Lombok @Getter 와 충돌하지 않도록 명시적으로 선언
    public boolean includeHeaders() { return includeHeaders; }
    public boolean includeBody() { return includeBody; }

    /** Formatter 가 compact 처리된 헤더 문자열을 기록한다 */
    public void headersValue(String v) { this.headersValue = v; }
    /** Formatter 가 compact 처리된 요청 body 문자열을 기록한다 */
    public void reqBodyValue(String v) { this.reqBodyValue = v; }
    /** Formatter 가 compact 처리된 응답 body 문자열을 기록한다 */
    public void resBodyValue(String v) { this.resBodyValue = v; }

    /** 예외가 있으면 simple class name, 없으면 null */
    public String exceptionSimpleNameOrNull() {
        return thrown == null ? null : thrown.getClass().getSimpleName();
    }
}