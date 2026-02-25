package com.example.api.filter;

import com.example.api.filter.logging.HttpLogContext;
import com.example.api.filter.logging.HttpLogFormatter;
import com.example.api.filter.logging.HttpLogPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p><b>HTTP 요청/응답 로깅 필터</b></p>
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>요청/응답의 <b>메타 정보</b>(method, uri, status, latency)를 1줄 로그로 남긴다.</li>
 *   <li>운영 디버깅을 위해 <b>Headers/Body</b>를 남길 수 있으나,
 *       기본 정책은 <b>에러(4xx/5xx/예외)/슬로우 요청</b>에서만 조건부로 확장한다.</li>
 *   <li>Body/Headers가 길면 <b>len + preview + gzip64</b> 형태로 축약(=compact)하여 로그 폭주를 방지한다.</li>
 * </ul>
 *
 *
 * <p><b>설계 포인트</b></p>
 * <ul>
 *   <li><code>ContentCachingRequestWrapper</code> / <code>ContentCachingResponseWrapper</code>를 사용해
 *       body를 읽더라도 실제 요청/응답 흐름을 깨지 않도록 한다.</li>
 *   <li>응답 래퍼를 사용하면 반드시 <code>copyBodyToResponse()</code>를 호출해야 클라이언트로 응답이 정상 전달된다.</li>
 *   <li>Async 요청은 REQUEST/ASYNC 디스패치가 여러 번 발생할 수 있으므로,
 *       <b>REQUEST 디스패치에서는 로깅을 스킵</b>하고 <b>ASYNC 디스패치(완료 시점)에만</b> 1회 로깅한다.</li>
 * </ul>
 *
 * <h2>보안 주의</h2>
 * <ul>
 *   <li><code>Authorization</code>, <code>Cookie</code>, <code>Set-Cookie</code> 등 민감 헤더는 마스킹한다.</li>
 *   <li>Body 로깅은 content-type을 제한하고(JSON/text 계열), JSON key 기반 마스킹을 적용한다.</li>
 *   <li>gzip64는 "압축"일 뿐 "비식별화"가 아니므로(복원 가능), 운영 정책/보관 기간/접근 통제를 반드시 고려한다.</li>
 * </ul>
 */
@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    // ---- wrapper/cache ----
    private static final int REQUEST_CACHE_LIMIT = 10_240;

    // ---- policy ----
    private static final boolean LOG_HEADERS = true;
    private static final boolean LOG_BODY_ON_ERROR_ONLY = true;
    private static final boolean LOG_BODY_ON_5XX = true;
    private static final long SLOW_THRESHOLD_MS = 2_000;

    // ---- formatter/compact ----
    private static final int PREVIEW_LEN = 256;
    private static final int MAX_HEADERS_INLINE_LENGTH = 512;
    private static final int MAX_BODY_INLINE_LENGTH = 2_048;
    private static final int MAX_GZIP64_LENGTH = 8_192;
    private static final int MAX_HEADER_VALUE_LENGTH = 256;

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "cookie", "set-cookie", "proxy-authorization"
    );

    // ⚠️ Authorization은 보통 allowlist에서 빼는 걸 추천(마스킹되더라도 노이즈)
    private static final List<String> ALLOWED_HEADERS = List.of(
            "content-type",
            "accept",
            "user-agent",
            "x-request-id",
            "x-forwarded-for",
            "x-real-ip",
            "referer",
            "host"
    );

    private static final Set<String> SENSITIVE_JSON_KEYS = Set.of(
            "password", "accesstoken"
    );

    private static final String LOGGED_ATTR = LoggingFilter.class.getName() + ".LOGGED";
    private static final String START_TIME_ATTR = LoggingFilter.class.getName() + ".START_TIME";

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("Asia/Seoul"));

    private final HttpLogPolicy policy;
    private final HttpLogFormatter formatter;

    public LoggingFilter(ObjectMapper objectMapper) {
        this.policy = new HttpLogPolicy(LOG_HEADERS, LOG_BODY_ON_ERROR_ONLY, LOG_BODY_ON_5XX, SLOW_THRESHOLD_MS);
        this.formatter = new HttpLogFormatter(
                objectMapper,
                PREVIEW_LEN,
                MAX_HEADERS_INLINE_LENGTH,
                MAX_BODY_INLINE_LENGTH,
                MAX_GZIP64_LENGTH,
                MAX_HEADER_VALUE_LENGTH,
                ALLOWED_HEADERS,
                lowerSet(SENSITIVE_HEADERS),
                lowerSet(SENSITIVE_JSON_KEYS)
        );
    }

    /**
     * <p>필터 메인 로직</p>
     * <ul>
     *   <li>Async dispatch는 스킵(중복 로그 및 상태 꼬임 방지)</li>
     *   <li>request/response wrapper 적용</li>
     *   <li>try/finally에서 로깅 및 <code>copyBodyToResponse()</code> 보장</li>
     * </ul>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("LoggingFilter.doFilterInternal() called");
            log.debug("Request URI: {} {}", request.getMethod(), request.getRequestURI());
        }

        if (request.getAttribute(START_TIME_ATTR) == null) {
            request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        }

        ContentCachingRequestWrapper req = wrapRequestIfNeeded(request);
        ContentCachingResponseWrapper res = wrapResponseIfNeeded(response);

        Throwable thrown = null;

        try {
            filterChain.doFilter(req, res);
        } catch (Throwable t) {
            thrown = t;
            throw t;
        } finally {
            // Async 완료 시점에만 로깅
            if (!isAsyncDispatch(request) && request.isAsyncStarted()) {
                safeCopyBodyToResponse(res);
                return;
            }

            // 한 요청당 한 번만
            if (Boolean.TRUE.equals(request.getAttribute(LOGGED_ATTR))) {
                safeCopyBodyToResponse(res);
                return;
            }

            long startMillis = (long) request.getAttribute(START_TIME_ATTR);
            long durationMs = System.currentTimeMillis() - startMillis;
            String startAt = TS_FMT.format(Instant.ofEpochMilli(startMillis));

            String uri = buildRequestUri(req);

            boolean debugEnabled = log.isDebugEnabled();

            HttpLogContext ctx = policy.decide(req, res, uri, startMillis, durationMs, startAt, thrown, debugEnabled);
            String line = formatter.format(req, res, ctx);

            HttpLogPolicy.Level level = policy.chooseLevel(ctx);
            if (level == HttpLogPolicy.Level.ERROR) {
                log.error(line, thrown);
            } else if (level == HttpLogPolicy.Level.WARN) {
                log.warn(line);
            } else {
                log.info(line);
            }

            request.setAttribute(LOGGED_ATTR, true);
            safeCopyBodyToResponse(res);
        }
    }

    /**
     * async-dispatch에서도 필터가 실행되게 해야 완료 시점(ASYNC dispatch)에서 로깅할 수 있다.
     * (REQUEST 디스패치에서는 isAsyncStarted() 조건으로 로깅을 스킵)
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /** 이미 래핑된 요청이면 재사용하고, 아니면 캐시 용량 제한을 걸어 새로 래핑한다 */
    private ContentCachingRequestWrapper wrapRequestIfNeeded(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper cached) return cached;
        return new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
    }

    /** 이미 래핑된 응답이면 재사용하고, 아니면 새로 래핑한다 */
    private ContentCachingResponseWrapper wrapResponseIfNeeded(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper cached) return cached;
        return new ContentCachingResponseWrapper(response);
    }

    /**
     * 응답 body 를 클라이언트로 전달한다.
     * 응답이 이미 커밋된 경우 {@link IllegalStateException} 이 발생할 수 있으므로 무시한다.
     */
    private void safeCopyBodyToResponse(ContentCachingResponseWrapper res) throws IOException {
        try {
            res.copyBodyToResponse();
        } catch (IllegalStateException ignored) {
        }
    }

    /** query string 이 있으면 포함한 전체 URI 를 반환한다 */
    private String buildRequestUri(HttpServletRequest request) {
        String qs = request.getQueryString();
        return request.getRequestURI() + (qs != null ? "?" + qs : "");
    }

    /** 문자열 집합의 모든 원소를 소문자로 변환한 불변 집합을 반환한다 */
    private static Set<String> lowerSet(Set<String> src) {
        return src.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

}
