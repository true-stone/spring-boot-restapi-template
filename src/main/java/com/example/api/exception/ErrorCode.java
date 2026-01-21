package com.example.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // COMMON
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.BAD_REQUEST, "C003", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "C005", "지원하지 않는 미디어 타입입니다."),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "요청하신 API 경로가 존재하지 않습니다."),

    // USER
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U001", "이미 사용 중인 아이디입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U003", "이미 사용 중인 이메일 주소입니다."),

    // AUTHENTICATION
    /** 포괄적인 인증 실패 */
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A001", "인증에 실패했습니다."),
    /** 로그인 실패 시 */
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A002", "아이디 또는 비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다."),
    /** 토큰이 없거나, 서명 오류 등 포괄적 의미 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A005", "만료된 토큰입니다."),

    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "A006", "비활성화된 계정입니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "A007", "잠긴 계정입니다. 관리자에게 문의하세요."),
    ACCOUNT_EXPIRED(HttpStatus.UNAUTHORIZED, "A008", "만료된 계정입니다."),
    CREDENTIALS_EXPIRED(HttpStatus.UNAUTHORIZED, "A009", "비밀번호 유효기간이 만료되었습니다."),

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
