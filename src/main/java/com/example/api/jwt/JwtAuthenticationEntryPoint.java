package com.example.api.jwt;

import com.example.api.dto.ErrorResponse;
import com.example.api.exception.ErrorCode;
import com.example.api.filter.JwtAuthenticationFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Object exceptionAttribute = request.getAttribute(JwtAuthenticationFilter.EXCEPTION_ATTRIBUTE_KEY);

        ErrorCode errorCode;

        if (exceptionAttribute instanceof ErrorCode) {
            errorCode = (ErrorCode) exceptionAttribute;
        } else {
            errorCode = ErrorCode.INVALID_TOKEN;
        }

        ErrorResponse errorResponse = ErrorResponse.of(errorCode);

        response.setStatus(errorResponse.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}