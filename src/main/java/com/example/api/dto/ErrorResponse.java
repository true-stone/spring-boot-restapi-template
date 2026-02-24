package com.example.api.dto;

import com.example.api.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "ErrorResponse", description = "에러 응답 형식")
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "에러 발생 시각", requiredMode = REQUIRED)
    private final LocalDateTime createdAt = LocalDateTime.now(); // 필드명 변경

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

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
    }

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.status = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
        this.detail = message;
    }

    public ErrorResponse(ErrorCode errorCode, List<FieldError> errors) {
        this.status = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
        this.errors = errors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode, errors);
    }

    @Schema(name = "ErrorResponse.FieldError", description = "필드 유효성 검증 오류 상세")
    public record FieldError(
            @Schema(
                    description = "필드 이름",
                    example = "password",
                    requiredMode = REQUIRED
            )
            String field,

            @Schema(
                    description = "필드 값",
                    example = "password123",
                    requiredMode = REQUIRED
            )
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
