package com.example.api.filter.logging;

import com.example.api.util.CompressUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HTTP 로그 라인 포맷터.
 *
 * <p>{@link HttpLogContext}의 정책 결정에 따라 헤더·body 를 추출·마스킹·compact 처리한 뒤
 * 1줄 텍스트 로그를 조립한다.</p>
 *
 * <p>compact 전략: 인라인 길이 초과 시 {@code {len, preview, gzip64}} 형태로 축약한다.
 * gzip64 도 최대 길이를 초과하면 {@code gzip64=omitted} 로 생략한다.</p>
 */
@RequiredArgsConstructor
public class HttpLogFormatter {

    private final ObjectMapper objectMapper;

    // ---- compact 임계값 ----
    private final int previewLen;
    private final int maxHeadersInlineLength;
    private final int maxBodyInlineLength;
    private final int maxGzip64Length;
    private final int maxHeaderValueLength;

    // ---- 헤더 정책 ----
    /** 출력을 허용할 헤더 이름 목록 (allowlist) */
    private final List<String> allowedHeaders;
    /** 값을 *** 로 마스킹할 헤더 이름 집합 (소문자) */
    private final Set<String> sensitiveHeadersLower;
    /** 값을 *** 로 마스킹할 JSON 키 집합 (소문자) */
    private final Set<String> sensitiveJsonKeysLower;

    /**
     * 컨텍스트 정책에 따라 헤더·body 를 포맷하고 1줄 로그 문자열을 반환한다.
     *
     * @param req  캐싱된 요청 래퍼
     * @param res  캐싱된 응답 래퍼
     * @param ctx  Policy 가 결정한 로그 컨텍스트
     * @return 완성된 1줄 로그 문자열
     */
    public String format(ContentCachingRequestWrapper req,
                         ContentCachingResponseWrapper res,
                         HttpLogContext ctx) {

        if (ctx.includeHeaders()) {
            String raw = formatHeaders(req);
            String compact = compactString(raw, maxHeadersInlineLength);
            if (!compact.isBlank()) ctx.headersValue(compact);
        }

        if (ctx.includeBody()) {
            String reqBody = extractRequestBody(req);
            String resBody = extractResponseBody(res);

            if (!reqBody.isBlank()) ctx.reqBodyValue(compactString(reqBody, maxBodyInlineLength));
            if (!resBody.isBlank()) ctx.resBodyValue(compactString(resBody, maxBodyInlineLength));
        }

        return buildLine(ctx);
    }

    /**
     * 컨텍스트 값을 조합하여 최종 로그 라인을 조립한다.
     * 형식: {@code [HTTP] METHOD URI -> STATUS (Nms) ts=... [slow=true] [ex=...] [headers=...] [req=...] [res=...]}
     */
    private String buildLine(HttpLogContext ctx) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("[HTTP] ")
                .append(ctx.getMethod()).append(' ')
                .append(ctx.getUri())
                .append(" -> ").append(ctx.getStatus())
                .append(" (").append(ctx.getDurationMs()).append("ms) ")
                .append("ts=").append(ctx.getStartedAtIso());

        if (ctx.isSlow()) sb.append(" slow=true");
        if (ctx.isException()) sb.append(" ex=").append(ctx.exceptionSimpleNameOrNull());

        if (ctx.getHeadersValue() != null && !ctx.getHeadersValue().isBlank()) {
            sb.append(" headers=").append(ctx.getHeadersValue());
        }
        if (ctx.getReqBodyValue() != null && !ctx.getReqBodyValue().isBlank()) {
            sb.append(" req=").append(ctx.getReqBodyValue());
        }
        if (ctx.getResBodyValue() != null && !ctx.getResBodyValue().isBlank()) {
            sb.append(" res=").append(ctx.getResBodyValue());
        }
        return sb.toString();
    }

    /**
     * allowedHeaders 목록 기준으로 헤더를 수집하고, 민감 헤더는 *** 로 마스킹한다.
     * 출력 형식: {@code {key=value, ...}}
     */
    private String formatHeaders(HttpServletRequest request) {
        Map<String, String> result = new LinkedHashMap<>();

        for (String headerName : allowedHeaders) {
            String value = request.getHeader(headerName);
            if (value == null) continue;

            String lower = headerName.toLowerCase(Locale.ROOT);
            String sanitizedValue = sensitiveHeadersLower.contains(lower)
                    ? "***"
                    : abbreviate(value, maxHeaderValueLength);

            result.put(lower, sanitizedValue);
        }

        return result.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /** 요청 body 를 캐시에서 읽어 문자열로 변환한다 */
    private String extractRequestBody(ContentCachingRequestWrapper request) {
        return extractBody(
                request.getContentType(),
                request.getCharacterEncoding(),
                request.getContentAsByteArray()
        );
    }

    /** 응답 body 를 캐시에서 읽어 문자열로 변환한다 */
    private String extractResponseBody(ContentCachingResponseWrapper response) {
        return extractBody(
                response.getContentType(),
                response.getCharacterEncoding(),
                response.getContentAsByteArray()
        );
    }

    /**
     * Content-Type 이 로깅 대상이고 body 가 있으면 정규화된 문자열을 반환한다.
     * 로깅 불가 타입(binary 등)이면 빈 문자열을 반환한다.
     */
    private String extractBody(String contentType, String encoding, byte[] bodyBytes) {
        if (!isLoggableContentType(contentType)) return "";
        if (bodyBytes == null || bodyBytes.length == 0) return "";

        Charset cs = resolveCharsetForJsonPreferred(contentType, encoding);
        return normalizeBody(new String(bodyBytes, cs), contentType);
    }

    /** JSON/text 계열만 로깅 대상으로 허용한다 (binary, multipart 등 제외) */
    private boolean isLoggableContentType(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase(Locale.ROOT);

        return ct.contains(MediaType.APPLICATION_JSON_VALUE)
                || ct.startsWith(MediaType.TEXT_PLAIN_VALUE)
                || ct.startsWith(MediaType.TEXT_HTML_VALUE)
                || ct.startsWith("application/xml")
                || ct.startsWith("text/");
    }

    /**
     * JSON 이면 Content-Type 헤더의 charset 을 우선 사용하고, 없으면 UTF-8 로 확정한다.
     * 그 외 타입은 응답/요청이 선언한 encoding 을 따른다.
     */
    private Charset resolveCharsetForJsonPreferred(String contentType, String encoding) {
        boolean isJson = contentType != null
                && contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE);

        if (isJson) {
            Charset fromHeader = charsetFromContentType(contentType);
            return (fromHeader != null) ? fromHeader : StandardCharsets.UTF_8;
        }

        if (encoding == null || encoding.isBlank()) return StandardCharsets.UTF_8;
        try {
            return Charset.forName(encoding);
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    /** Content-Type 헤더에서 charset 파라미터를 파싱한다. 파싱 실패 시 null 반환 */
    private Charset charsetFromContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) return null;
        try {
            MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);
            return mimeType.getCharset();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * body 문자열을 로그 출력에 적합한 형태로 정규화한다.
     * JSON 이면 minify + 민감 키 마스킹, 그 외 타입은 제어 문자를 이스케이프한다.
     */
    private String normalizeBody(String body, String contentType) {
        if (body == null || body.isBlank()) return "";

        boolean isJson = contentType != null
                && contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE);

        return isJson ? minifyJsonSafely(body) : sanitizeControls(body);
    }

    /**
     * JSON 을 파싱해 민감 키를 마스킹하고 minify 한다.
     * 파싱 실패 시 개행 문자만 제거한 원문을 반환한다.
     */
    private String minifyJsonSafely(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            maskSensitiveJson(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return raw.replace("\r", "").replace("\n", "");
        }
    }

    /**
     * JSON 트리를 재귀 순회하며 {@code sensitiveJsonKeysLower} 에 등록된 키의 값을 *** 로 치환한다.
     * Object 와 Array 모두 처리한다.
     */
    private void maskSensitiveJson(JsonNode node) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            for (Map.Entry<String, JsonNode> entry : obj.properties()) {
                String key = entry.getKey();
                JsonNode value = entry.getValue();

                if (isSensitiveKey(key)) {
                    obj.put(key, "***");
                    continue;
                }
                maskSensitiveJson(value);
            }
            return;
        }

        if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (JsonNode child : arr) {
                maskSensitiveJson(child);
            }
        }
    }

    /** 키 이름이 민감 목록에 포함되는지 대소문자 무관으로 확인한다 */
    private boolean isSensitiveKey(String key) {
        if (key == null) return false;
        return sensitiveJsonKeysLower.contains(key.toLowerCase(Locale.ROOT));
    }

    /** 제어 문자(개행·탭)를 가시적인 이스케이프 문자열로 치환한다 */
    private String sanitizeControls(String s) {
        return s.replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    /**
     * 문자열이 {@code inlineLimit} 을 초과하면 compact 형태({@code {len, preview, gzip64}})로 축약한다.
     * gzip64 결과가 {@code maxGzip64Length} 를 초과하면 {@code gzip64=omitted} 로 생략한다.
     */
    private String compactString(String value, int inlineLimit) {
        if (value == null || value.isBlank()) return "";

        String normalized = sanitizeControls(value);

        if (normalized.length() <= inlineLimit) {
            return normalized;
        }

        int len = normalized.length();
        String preview = abbreviate(normalized, previewLen);

        String gzip64 = CompressUtils.gzipBase64(normalized);
        if (gzip64.isBlank() || gzip64.length() > maxGzip64Length) {
            return "{len=" + len + ", preview=" + preview + ", gzip64=omitted}";
        }
        return "{len=" + len + ", preview=" + preview + ", gzip64=" + gzip64 + "}";
    }

    /** 문자열을 maxLen 이하로 자르고 초과 시 {@code ...(truncated)} 를 덧붙인다 */
    private String abbreviate(String value, int maxLen) {
        if (value == null) return "";
        if (value.length() <= maxLen) return value;
        return value.substring(0, maxLen) + "...(truncated)";
    }

}