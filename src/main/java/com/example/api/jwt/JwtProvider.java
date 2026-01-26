package com.example.api.jwt;

import com.example.api.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

    public static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    public static final String TOKEN_PREFIX = "Bearer ";

    private static final String AUTHORITIES_KEY = "role";

    private final SecretKey secretKey;
    private final long accessTokenExpireMilliseconds;
    private final long refreshTokenExpireMilliseconds;
    private final JwtParser jwtParser;

    public JwtProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-expire-seconds}") long accessTokenExpireSeconds,
            @Value("${jwt.refresh-token-expire-seconds}") long refreshTokenExpireSeconds
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpireMilliseconds = accessTokenExpireSeconds * 1000L;
        this.refreshTokenExpireMilliseconds = refreshTokenExpireSeconds * 1000L;
        this.jwtParser = Jwts.parser().verifyWith(this.secretKey).build();
    }

    /**
     * HTTP 요청으로 부터 토큰 추출
     *
     * @param request HTTP 요청
     * @return 토큰
     */
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return  bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * AccessToken 생성
     *
     * @param authentication 인증 정보
     * @return AccessToken
     */
    public String generateAccessToken(Authentication authentication) {
        UUID publicId = extractPublicId(authentication);

        // 권한
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a != null && a.startsWith("ROLE_"))   // ✅ 핵심
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));

        // 생성일 & 만료일
        Date now = new Date();
        Date expiresIn = new Date(now.getTime() + this.accessTokenExpireMilliseconds);

        return Jwts.builder()
                .subject(publicId.toString())
                .claim(AUTHORITIES_KEY, roles)
                .signWith(secretKey)
                .issuedAt(now)
                .expiration(expiresIn)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        String roles = Optional.ofNullable(claims.get(AUTHORITIES_KEY))
                .map(Object::toString)
                .orElse("")
                .trim();

        List<GrantedAuthority> authorities =
                StringUtils.hasText(roles)
                        ? Arrays.stream(roles.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .filter(a -> a.startsWith("ROLE_"))
                        .map(a -> (GrantedAuthority) new SimpleGrantedAuthority(a))
                        .toList()
                        : List.of();

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public long getAccessTokenExpireSeconds() {
        return accessTokenExpireMilliseconds / 1000L;
    }

    public Claims getClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    private UUID extractPublicId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication is null");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl(com.example.api.entity.User user)) {
            return user.getPublicId();
        }

        throw new IllegalStateException(
                "Unsupported principal type: " + (principal == null ? "null" : principal.getClass().getName())
                        + ", name=" + authentication.getName()
        );
    }
}
