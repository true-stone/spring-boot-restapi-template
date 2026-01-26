package com.example.api.filter;

import com.example.api.exception.ErrorCode;
import com.example.api.jwt.JwtProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰을 검증하고, 유효한 경우 Spring Security의 SecurityContext에 인증 정보를 설정하는 필터입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String EXCEPTION_ATTRIBUTE_KEY = "exception";
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtProvider.getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                jwtProvider.getClaims(token);

                // 예외가 발생하지 않았다면, 토큰이 유효한 것이므로 인증 정보를 설정합니다.
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                // 만료된 토큰 예외 처리
                request.setAttribute(EXCEPTION_ATTRIBUTE_KEY, ErrorCode.TOKEN_EXPIRED);
            } catch (JwtException | IllegalArgumentException e) {
                // 유효하지 않은 토큰
                request.setAttribute(EXCEPTION_ATTRIBUTE_KEY, ErrorCode.INVALID_TOKEN);
            }
        }

        filterChain.doFilter(request, response);
    }
}