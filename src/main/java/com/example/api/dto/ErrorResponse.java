package com.example.api.dto;

import com.example.api.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final LocalDateTime dateTime = LocalDateTime.now();
    private final int status;
    private final String code;
    private final String message;
    private String detail;
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

    public record FieldError(String field, String value, String reason) {

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
