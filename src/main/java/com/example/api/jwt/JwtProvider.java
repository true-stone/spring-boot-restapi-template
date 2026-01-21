package com.example.api.jwt;

import io.jsonwebtoken.*;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
        // 권한
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 생성일 & 만료일
        Date now = new Date();
        Date expiresIn = new Date(now.getTime() + this.accessTokenExpireMilliseconds);

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(secretKey)
                .issuedAt(now)
                .expiration(expiresIn)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

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
}
