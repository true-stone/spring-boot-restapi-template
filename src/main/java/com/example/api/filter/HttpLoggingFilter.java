package com.example.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.example.api.filter.logging.RequestContextConstants.*;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final int REQUEST_BODY_MAX_BYTES = 10_240;
    private static final int BODY_LOG_MAX_CHARS = 2_000;

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of("Asia/Seoul"));

    private final ObjectMapper objectMapper;

    public HttpLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        // ASYNC 디스패치(완료 시점)에서 로그를 남기기 위해 필터를 허용
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        // 에러 디스패치도 필터를 태워 최종 status 기준 접근 로그를 남길 수 있게 허용
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 최초 디스패치에서만 시작 시간·requestId 초기화
        // (ERROR 디스패치 포함 재진입 시 덮어쓰지 않도록 null 체크)
        if (request.getAttribute(START_TIME_ATTR) == null) {
            request.setAttribute(START_TIME_ATTR, Instant.now());
            String requestId = getOrCreateRequestId(request);
            request.setAttribute(REQUEST_ID_ATTR, requestId);
            response.setHeader(REQUEST_ID_HEADER, requestId);
        }

        // MDC는 스레드 로컬이므로 매 디스패치마다 설정
        MDC.put(MDC_REQUEST_ID_KEY, (String) request.getAttribute(REQUEST_ID_ATTR));

        ContentCachingRequestWrapper req = wrapRequest(request);
        ContentCachingResponseWrapper res = wrapResponse(response);

        Throwable thrown = null;
        try {
            filterChain.doFilter(req, res);
        } catch (Exception e) {
            thrown = e;
            throw e;
        } finally {
            try {
                // 동기 요청: REQUEST 디스패치 완료 후 바로 로깅
                // 비동기 요청: ASYNC 디스패치(완료 시점)에서 로깅
                // LOGGED_ATTR로 ERROR 디스패치 등 재진입 시 이중 로깅 방지
                if (!req.isAsyncStarted() && request.getAttribute(LOGGED_ATTR) == null) {
                    request.setAttribute(LOGGED_ATTR, Boolean.TRUE);

                    // 로그 직전에 REQUEST_ID_ATTR 기준으로 MDC를 재설정
                    // filterChain 안의 다른 필터가 MDC를 변경했을 가능성을 차단
                    MDC.put(MDC_REQUEST_ID_KEY, (String) request.getAttribute(REQUEST_ID_ATTR));

                    Instant startTime = (Instant) request.getAttribute(START_TIME_ATTR);
                    long latencyMs = Duration.between(startTime, Instant.now()).toMillis();
                    int status = res.getStatus();
                    String method = req.getMethod();
                    String uri = buildRequestUri(req);
                    String requestedAt = TS_FMT.format(startTime);
                    String clientIp = extractClientIp(req);

                    StringBuilder sb = new StringBuilder("[HTTP]")
                            .append(" method=").append(method)
                            .append(" uri=").append(uri)
                            .append(" status=").append(status)
                            .append(" latencyMs=").append(latencyMs)
                            .append(" requestedAt=").append(requestedAt)
                            .append(" clientIp=").append(clientIp);

                    if (status >= 400) {
                        sb.append(" reqBody=").append(readBody(req.getContentAsByteArray(), req.getContentType()))
                                .append(" resBody=").append(readBody(res.getContentAsByteArray(), res.getContentType()));
                    }

                    String line = sb.toString();

                    if (status >= 500) {
                        log.error(line, thrown);
                    } else if (status >= 400) {
                        log.warn(line);
                    } else {
                        log.info(line);
                    }
                }
            } finally {
                res.copyBodyToResponse();
                MDC.remove(MDC_REQUEST_ID_KEY);
            }
        }
    }

    private String getOrCreateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString().substring(0, 8);
        }
        return requestId;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String buildRequestUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper wrapper) return wrapper;
        return new ContentCachingRequestWrapper(request, REQUEST_BODY_MAX_BYTES);
    }

    private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper wrapper) return wrapper;
        return new ContentCachingResponseWrapper(response);
    }

    /**
     * byte[]로 캐시된 body를 문자열로 변환한다.
     * JSON이면 ObjectMapper로 minify하고, text 계열은 개행만 제거한다.
     * binary 타입이면 [binary]를 반환하고, BODY_LOG_MAX_CHARS 초과 시 잘라낸다.
     */
    private String readBody(byte[] content, String contentType) {
        if (content == null || content.length == 0) return "";
        if (contentType == null) return "[binary]";
        String ct = contentType.toLowerCase();
        if (!ct.contains("json") && !ct.startsWith("text/")) return "[binary]";

        String raw = new String(content, StandardCharsets.UTF_8);
        String body = ct.contains("json") ? minifyJson(raw) : raw.replaceAll("\\s*[\\r\\n]+\\s*", "");

        if (body.length() > BODY_LOG_MAX_CHARS) {
            return body.substring(0, BODY_LOG_MAX_CHARS) + "...(truncated)";
        }
        return body;
    }

    /**
     * JSON 파싱 후 재직렬화로 minify. 파싱 실패 시 개행 제거한 원문을 반환한다.
     */
    private String minifyJson(String raw) {
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(raw));
        } catch (Exception e) {
            return raw.replaceAll("\\s*[\\r\\n]+\\s*", "");
        }
    }
}
