package com.example.api.config;

import com.example.api.jwt.JwtAccessDeniedHandler;
import com.example.api.jwt.JwtAuthenticationEntryPoint;
import com.example.api.filter.JwtAuthenticationFilter;
import com.example.api.security.PublicEndpoints;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toH2Console;
import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toStaticResources;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                // 모바일 네이티브 앱 전용이면 CORS는 의미가 거의 없으므로 disable
                .cors(AbstractHttpConfigurer::disable)

                // 토큰(Authorization 헤더) 기반이면 CSRF 불필요
                .csrf(AbstractHttpConfigurer::disable)

                // 폼 로그인/HTTP Basic 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션 미사용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // 인가(Authorization) 정책
                .authorizeHttpRequests(authorize -> authorize

                        // 공개 엔드포인트 (상황에 맞게 조정)
                        .requestMatchers(toH2Console(), toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(PublicEndpoints.COMMON).permitAll()
                        .requestMatchers(PublicEndpoints.AUTH).permitAll()
                        .requestMatchers(HttpMethod.POST, PublicEndpoints.USER).permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                // 검증 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }

    /**
     * 인증과 인가에 사용될 패스워드 인코딩 방식을 지정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증을 총괄하는 AuthenticationManager를 생성
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
