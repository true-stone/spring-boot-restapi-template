package com.example.api.security;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PermitAllPolicy {

    private static final String API_PREFIX = "/api/*";

    /**
     * Swagger/OpenAPI
     */
    private static final List<String> SWAGGER = List.of(
            "/v3/api-docs/**",
            "/swagger-ui/**"
    );

    /**
     * 공통 공개 API
     */
    private static final List<String> COMMON = List.of(
            "/common/**"
    );

    /**
     * Auth 공개 API
     */
    private static final List<String> AUTH = List.of(
            API_PREFIX + "/auth/**"
    );

    /**
     * 회원가입(POST /api/{version}/users)
     */
    private static final List<MethodAndPath> USER_SIGN_UP = List.of(
            MethodAndPath.of(HttpMethod.POST, API_PREFIX + "/users")
    );

    public static String[] swaggerPaths() {
        return SWAGGER.toArray(String[]::new);
    }

    public static String[] commonPaths() {
        return COMMON.toArray(String[]::new);
    }

    public static String[] authPaths() {
        return AUTH.toArray(String[]::new);
    }

    public static List<MethodAndPath> userSignUp() {
        return USER_SIGN_UP;
    }

    /**
     * (method, path) 조합
     */
    public record MethodAndPath(HttpMethod method, String path) {
        public static MethodAndPath of(HttpMethod method, String path) {
            return new MethodAndPath(method, path);
        }
    }
}
