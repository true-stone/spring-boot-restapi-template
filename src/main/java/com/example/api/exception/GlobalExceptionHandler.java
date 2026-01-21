package com.example.api.exception;

import com.example.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Optional;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 클래스입니다.
 * ResponseEntityExceptionHandler를 상속받아 Spring MVC의 기본 예외 처리 기능을 확장합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * <code>@Valid</code> 유효성 검증 실패 시 발생하는 예외를 처리합니다. (400 Bad Request)
     * 상세한 유효성 검증 실패 메시지를 추출하여 응답에 포함시킵니다.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ErrorResponse.FieldError> fieldErrors =
                ex.getBindingResult().getFieldErrors()
                        .stream()
                        .map(ErrorResponse.FieldError::of)
                        .toList();

        if (log.isDebugEnabled()) {
            log.debug("validation_failed path={} method={} errorCode={} fieldErrors={}",
                    extractPath(request),
                    extractMethod(request),
                    ErrorCode.INVALID_INPUT_VALUE.getCode(),
                    fieldErrors.stream()
                            .map(fe -> fe.field() + ":" + fe.reason()) // 예: record에 field/reason이 있다고 가정
                            .toList()
            );
        }

        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, fieldErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * HTTP 요청의 본문을 읽을 수 없을 때 발생하는 예외를 처리합니다. (400 Bad Request)
     * 주로 JSON 형식의 요청 본문이 비어있거나, 형식이 잘못되었을 때 발생합니다.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        if (log.isDebugEnabled()) {
            log.debug("Invalid HTTP request body for request [{}]: {}", request.getDescription(false), ex.getMessage());
        }

        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "요청 본문의 형식이 잘못되었거나 비어있습니다.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 컨트롤러의 <code>@RequestParam</code>, <code>@PathVariable</code> 등에서 타입 변환 실패 시 발생하는 예외를 처리합니다. (400 Bad Request)
     * ex) Long 타입의 id에 문자열 "abc"를 전달하는 경우.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (log.isDebugEnabled()) {
            String method = null;
            String uri = null;

            if (request instanceof ServletWebRequest swr) {
                method = swr.getHttpMethod() != null ? swr.getHttpMethod().name() : null;
                uri = swr.getRequest().getRequestURI();
            }

            String param = ex.getPropertyName();
            Object valueObj = ex.getValue();
            String value = valueObj == null ? "null" : String.valueOf(valueObj);
            if (value.length() > 200) { // 과도한 길이 방지
                value = value.substring(0, 200) + "...";
            }

            String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

            log.debug("Type mismatch [{} {}] | param={} | value={} | requiredType={} | msg={}",
                    method, uri, param, value, requiredType, ex.getMessage(), ex);
        }

        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

        String detail = String.format(
                "파라미터 '%s'에 잘못된 타입의 값('%s')이 입력되었습니다. (필요한 타입: '%s')",
                ex.getPropertyName(),
                ex.getValue(),
                requiredType
        );

        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, detail);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 필수 요청 파라미터(<code>@RequestParam</code>)가 누락되었을 때 발생하는 예외를 처리합니다. (400 Bad Request)
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (log.isDebugEnabled()) {
            String method = null;
            String uri = null;
            String query = null;

            if (request instanceof ServletWebRequest swr) {
                var httpReq = swr.getRequest();
                method = httpReq.getMethod();
                uri = httpReq.getRequestURI();
                query = httpReq.getQueryString();
            }

            // query는 길어질 수 있어 제한 (로그 오염 방지)
            if (query != null && query.length() > 300) {
                query = query.substring(0, 300) + "...";
            }

            log.debug(
                    "Missing required parameter [{} {}] | param={} | requiredType={} | query={} | msg={}",
                    method,
                    uri,
                    ex.getParameterName(),
                    ex.getParameterType(),
                    query,
                    ex.getMessage(),
                    ex // 필요 시 스택트레이스
            );
        }

        String detail = String.format(
                "필수 파라미터 '%s'(타입: %s)가 누락되었습니다.",
                ex.getParameterName(),
                ex.getParameterType()
        );

        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, detail);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * {@code @RequestParam}, {@code PathVariable}, {@code @RequestHeader} 등 컨트롤러 메서드의 파라미터 유효성 검증 실패 시 발생하는 예외를 처리합니다. (400 Bad Request)
     * <pre>
     * 이 예외는 컨트롤러 클래스에 {@code @Validated} 어노테이션이 붙어있고, 메서드 파라미터에 유효성 검증 어노테이션(예: @NotBlank, @Min)이 사용되었을 때 발생합니다.
     * 실패한 모든 파라미터에 대한 상세 정보를 {@code errors} 필드에 포함하여 반환합니다.
     * </pre>
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErrorResponse.FieldError> fieldErrors =
                ex.getParameterValidationResults().stream()
                        .flatMap(result ->
                                result.getResolvableErrors().stream()
                                        .map(error -> new ErrorResponse.FieldError(
                                                result.getMethodParameter().getParameterName(),
                                                extractRejectedValue(error),
                                                error.getDefaultMessage()
                                        ))
                        )
                        .toList();

        if (log.isDebugEnabled()) {
            log.debug(
                    "handler_method_validation_failed path={} method={} fieldErrors={}",
                    extractPath(request),
                    extractMethod(request),
                    fieldErrors.stream()
                            .map(fe -> fe.field() + ":" + fe.reason())
                            .toList()
            );
        }

        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, fieldErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 존재하지 않는 URL로 요청 시 발생하는 예외를 처리합니다. (404 Not Found)
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (log.isDebugEnabled()) {
            String method = null;
            String uri = null;
            String query = null;

            if (request instanceof ServletWebRequest swr) {
                var httpReq = swr.getRequest();
                method = httpReq.getMethod();
                uri = httpReq.getRequestURI();
                query = httpReq.getQueryString();
            }

            if (query != null && query.length() > 300) {
                query = query.substring(0, 300) + "...";
            }

            // NoHandlerFoundException 자체에도 method/url이 있으니 우선 사용
            String exMethod = ex.getHttpMethod();
            String exUrl = ex.getRequestURL();

            log.debug(
                    "No handler found [{} {}] | exMethod={} | exUrl={} | query={} | msg={}",
                    method,
                    uri,
                    exMethod,
                    exUrl,
                    query,
                    ex.getMessage()
            );
        }

        final ErrorResponse response = ErrorResponse.of(ErrorCode.ENDPOINT_NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * 지원하지 않는 HTTP 메서드로 요청 시 발생하는 예외를 처리합니다. (405 Method Not Allowed)
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.debug("Unsupported HTTP method for request [{} {}]: {}", ex.getMethod(), request.getDescription(false), ex.getMessage());

        headers.setAllow(ex.getSupportedHttpMethods());

        String detail = String.format("요청하신 '%s' 메서드는 지원하지 않습니다. (지원하는 메서드: %s)",
                ex.getMethod(),
                ex.getSupportedHttpMethods());

        final ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, detail);
        return new ResponseEntity<>(response, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 지원하지 않는 미디어 타입(Content-Type)으로 요청 시 발생하는 예외를 처리합니다. (415 Unsupported Media Type)
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.debug("Unsupported media type for request [{}]: {}", request.getDescription(false), ex.getContentType());

        String detail = String.format("지원하지 않는 미디어 타입('%s')입니다. ('%s' 타입으로 요청해주세요.)",
                ex.getContentType(),
                ex.getSupportedMediaTypes());

        final ErrorResponse response = ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE, detail);
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * 인증 과정에서 발생하는 예외(AuthenticationException)를 처리합니다. (401 Unauthorized)
     * 로그인 실패(아이디/비밀번호 불일치, 계정 비활성화 등) 시 호출됩니다.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest request) {

        if (log.isDebugEnabled()) {
            Throwable cause = ex.getCause();

            String causePart = (cause != null)
                    ? String.format(" | cause=%s : %s",
                    cause.getClass().getSimpleName(),
                    cause.getMessage())
                    : "";

            log.debug(
                    "Auth failed [{} {}] | ex={} | msg={}{}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    causePart,
                    ex
            );
        }

        ErrorCode errorCode = switch (ex) {
            case UsernameNotFoundException e -> ErrorCode.INVALID_CREDENTIALS;
            case BadCredentialsException e -> ErrorCode.INVALID_CREDENTIALS;
            case DisabledException e -> ErrorCode.ACCOUNT_DISABLED;
            case LockedException e -> ErrorCode.ACCOUNT_LOCKED;
            case AccountExpiredException e -> ErrorCode.ACCOUNT_EXPIRED;
            case CredentialsExpiredException e -> ErrorCode.CREDENTIALS_EXPIRED;
            default -> ErrorCode.AUTHENTICATION_FAILED;
        };

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * 애플리케이션의 비즈니스 로직에서 발생하는 예외(BusinessException)를 처리합니다.
     * ErrorCode에 정의된 HTTP 상태 코드와 메시지를 반환합니다.
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();

        if (log.isDebugEnabled()) {
            if (errorCode.getStatus().is5xxServerError()) {
                log.error("Business exception occurred for request [{} {}]: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        errorCode.getMessage(),
                        ex);
            } else {
                log.debug("Business exception occurred for request [{} {}]: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        errorCode.getMessage());
            }
        }

        final ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * 위에서 처리하지 못한 모든 서버 내부 예외를 처리합니다. (500 Internal Server Error)
     * 스택 트레이스 전체를 로깅하여 원인 분석을 용이하게 합니다.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("An unexpected error occurred for request [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String extractPath(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            HttpServletRequest req = swr.getRequest();
            return req.getRequestURI();
        }
        return "N/A";
    }

    private static String extractMethod(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return Optional.ofNullable(swr.getHttpMethod())
                    .map(HttpMethod::name)
                    .orElse("N/A");
        }
        return "N/A";
    }

    private static String extractRejectedValue(Object error) {
        if (error instanceof org.springframework.validation.FieldError fe) {
            return Optional.ofNullable(fe.getRejectedValue())
                    .map(Object::toString)
                    .orElse("");
        }
        return "";
    }
}
