package com.example.api.dto;

import com.example.api.exception.ErrorCode;
import com.example.api.filter.logging.RequestContextConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "에러 응답 형식")
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Schema(description = "요청 시작 시간", requiredMode = REQUIRED)
    private final LocalDateTime requestedAt;

    @Schema(description = "요청 추적 ID", example = "a1b2c3d4", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String requestId;

    @Schema(description = "HTTP 상태 코드", example = "400", requiredMode = REQUIRED)
    private final int status;

    @Schema(description = "에러 메시지", example = "입력값이 올바르지 않습니다.", requiredMode = REQUIRED)
    private final String message;

    @Schema(description = "내부 에러 코드", example = "C001", requiredMode = REQUIRED)
    private final String code;

    @Schema(description = "상세 에러 설명", example = "구체적인 에러 설명")
    private String detail;

    @Schema(description = "필드별 유효성 검증 오류 목록 (유효성 검증 실패 시에만 포함)")
    private List<FieldError> errors;

    private ErrorResponse(
            LocalDateTime requestedAt,
            String requestId,
            ErrorCode errorCode,
            String detail,
            List<FieldError> errors
    ) {
        this.requestedAt = requestedAt;
        this.requestId = requestId;
        this.status = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
        this.detail = detail;
        this.errors = errors;
    }

    private ErrorResponse(ErrorCode errorCode) {
        this(LocalDateTime.now(SEOUL_ZONE_ID), null, errorCode, null, null);
    }

    private ErrorResponse(ErrorCode errorCode, String detail) {
        this(LocalDateTime.now(SEOUL_ZONE_ID), null, errorCode, detail, null);
    }

    private ErrorResponse(ErrorCode errorCode, List<FieldError> errors) {
        this(LocalDateTime.now(SEOUL_ZONE_ID), null, errorCode, null, errors);
    }

    public ErrorResponse(HttpServletRequest request, ErrorCode errorCode) {
        this(resolveRequestedAt(request), resolveRequestId(request), errorCode, null, null);
    }

    public ErrorResponse(HttpServletRequest request, ErrorCode errorCode, String detail) {
        this(resolveRequestedAt(request), resolveRequestId(request), errorCode, detail, null);
    }

    public ErrorResponse(HttpServletRequest request, ErrorCode errorCode, List<FieldError> errors) {
        this(resolveRequestedAt(request), resolveRequestId(request), errorCode, null, errors);
    }

    /** Swagger 예제 등 실제 요청 컨텍스트 없이 ErrorCode만으로 생성할 때 사용 */
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(LocalDateTime.now(SEOUL_ZONE_ID), "a1b2c3d4", errorCode, null, null);
    }

    public static ErrorResponse of(HttpServletRequest request, ErrorCode errorCode) {
        return new ErrorResponse(request, errorCode);
    }

    public static ErrorResponse of(HttpServletRequest request, ErrorCode errorCode, String detail) {
        return new ErrorResponse(request, errorCode, detail);
    }

    public static ErrorResponse of(HttpServletRequest request, ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(request, errorCode, errors);
    }

    public static ErrorResponse of(WebRequest request, ErrorCode errorCode) {
        HttpServletRequest httpServletRequest = toHttpServletRequest(request);
        return httpServletRequest != null ? of(httpServletRequest, errorCode) : new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(WebRequest request, ErrorCode errorCode, String detail) {
        HttpServletRequest httpServletRequest = toHttpServletRequest(request);
        return httpServletRequest != null ? of(httpServletRequest, errorCode, detail) : new ErrorResponse(errorCode, detail);
    }

    public static ErrorResponse of(WebRequest request, ErrorCode errorCode, List<FieldError> errors) {
        HttpServletRequest httpServletRequest = toHttpServletRequest(request);
        return httpServletRequest != null ? of(httpServletRequest, errorCode, errors) : new ErrorResponse(errorCode, errors);
    }

    private static LocalDateTime resolveRequestedAt(HttpServletRequest request) {
        Object startTime = request.getAttribute(RequestContextConstants.START_TIME_ATTR);
        if (startTime instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, SEOUL_ZONE_ID);
        }
        return LocalDateTime.now(SEOUL_ZONE_ID);
    }

    private static String resolveRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestContextConstants.REQUEST_ID_ATTR);
        if (requestId instanceof String value && !value.isBlank()) {
            return value;
        }

        String mdcRequestId = MDC.get(RequestContextConstants.MDC_REQUEST_ID_KEY);
        if (mdcRequestId != null && !mdcRequestId.isBlank()) {
            return mdcRequestId;
        }

        return "";
    }

    private static HttpServletRequest toHttpServletRequest(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest();
        }
        return null;
    }

    @Schema(description = "필드 유효성 검증 오류 상세")
    public record FieldError(
            @Schema(description = "필드 이름", example = "password", requiredMode = REQUIRED)
            String field,

            @Schema(description = "필드 값", example = "password123", requiredMode = REQUIRED)
            String value,

            @Schema(
                    description = "유효성 검증 실패 이유",
                    example = "비밀번호는 영문 소문자, 대문자, 숫자, 특수문자를 포함해야 합니다.",
                    requiredMode = REQUIRED)
            String reason
    ) {

        public static FieldError of(org.springframework.validation.FieldError fieldError) {
            return new FieldError(
                    fieldError.getField(),
                    Optional.ofNullable(fieldError.getRejectedValue())
                            .map(Object::toString)
                            .orElse(""),
                    fieldError.getDefaultMessage()
            );
        }

        public static FieldError of(String field, Object value, String reason) {
            return new FieldError(
                    field,
                    value != null ? value.toString() : "",
                    reason
            );
        }
    }
}
