package com.example.api.filter.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

/**
 * HTTP 로그 출력 정책 결정기.
 *
 * <p>요청/응답의 상태(status, latency, exception)를 분석하여
 * 헤더·body 포함 여부와 로그 레벨을 결정한다.
 * 민감 정보 노출을 최소화하기 위해 401/403/404 는 headers·body 를 무조건 제외한다.</p>
 */
@AllArgsConstructor
public class HttpLogPolicy {

    /** 로그 레벨 선택에 사용되는 열거형 */
    public enum Level {INFO, WARN, ERROR}

    private final boolean logHeaders;
    private final boolean logBodyOnErrorOnly;
    private final boolean logBodyOn5xx;
    private final long slowThresholdMs;

    /**
     * 요청/응답 메타를 분석하여 {@link HttpLogContext}를 생성한다.
     *
     * @param req          요청 (method, uri 추출용)
     * @param res          응답 (status 추출용)
     * @param uri          query string 이 포함된 전체 요청 URI
     * @param startMillis  요청 시작 epoch ms
     * @param durationMs   처리 소요 시간
     * @param startedAtIso 요청 시작 시각 ISO-8601 문자열
     * @param thrown       필터 체인에서 발생한 예외 (없으면 null)
     * @param debugEnabled DEBUG 레벨 활성화 여부 (true 이면 body 강제 포함)
     */
    public HttpLogContext decide(HttpServletRequest req,
                                 HttpServletResponse res,
                                 String uri,
                                 long startMillis,
                                 long durationMs,
                                 String startedAtIso,
                                 Throwable thrown,
                                 boolean debugEnabled) {

        int status = res.getStatus();

        boolean is4xx = status >= 400 && status < 500;
        boolean is5xx = status >= 500;
        boolean isException = thrown != null;
        boolean isError = is4xx || is5xx || isException;
        boolean isSlow = durationMs >= slowThresholdMs;

        // 정상 응답(2xx/3xx)에서는 헤더를 출력하지 않는다
        boolean includeHeaders = logHeaders && (isError || isSlow);

        // DEBUG 모드이거나, 5xx/예외 발생 시 body 포함
        boolean includeBody =
                debugEnabled
                        || (
                        (!logBodyOnErrorOnly || isError)
                                && (logBodyOn5xx && (is5xx || isException))
                );

        // 401/403/404 는 민감 정보·노이즈가 될 수 있으므로 headers·body 제외
        if (status == 401 || status == 403 || status == 404) {
            includeHeaders = false;
            includeBody = false;
        }

        return new HttpLogContext(
                req.getMethod(),
                uri,
                status,
                startMillis,
                durationMs,
                startedAtIso,
                is4xx,
                is5xx,
                isException,
                isError,
                isSlow,
                thrown,
                includeHeaders,
                includeBody
        );
    }

    /**
     * 컨텍스트 상태에 따라 로그 레벨을 결정한다.
     * <ul>
     *   <li>5xx 또는 예외 → ERROR</li>
     *   <li>4xx 또는 슬로우 → WARN</li>
     *   <li>그 외 → INFO</li>
     * </ul>
     */
    public Level chooseLevel(HttpLogContext ctx) {
        if (ctx.is5xx() || ctx.isException()) return Level.ERROR;
        if (ctx.is4xx() || ctx.isSlow()) return Level.WARN;
        return Level.INFO;
    }
}