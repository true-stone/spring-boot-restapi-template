package com.example.api.config;

import com.example.api.filter.JwtAuthenticationFilter;
import com.example.api.jwt.JwtAccessDeniedHandler;
import com.example.api.jwt.JwtAuthenticationEntryPoint;
import com.example.api.security.PermitAllPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
                // 모바일 네이티브 앱 전용이면 CORS는 의미가 거의 없음(disable)
                // .cors(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                // 토큰(Authorization 헤더) 기반이면 CSRF 불필요
                .csrf(AbstractHttpConfigurer::disable)

                // 폼 로그인/HTTP Basic 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션 미사용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // 인가(Authorization) 정책
                .authorizeHttpRequests(authorize -> {
                    permitAllEndpoints(authorize);   // ✅ 공개 엔드포인트
                    authorize.anyRequest().authenticated(); // 그 외는 인증 필요
                })

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

    /**
     * 인증 없이 접근을 허용(permitted)할 엔드포인트들을 한 곳에 모아 정의
     *
     * <p>목적</p>
     * <ul>
     *   <li>SecurityConfig에서 permitAll 규칙을 흩뿌리지 않고 “공개 정책”을 한 메서드로 캡슐화</li>
     *   <li>인증 필요/불필요 경계를 명확히 하여 운영 중 정책 누락/중복을 줄임</li>
     *   <li>브라우저 기반 호출(React 등)에서 필요한 CORS Preflight(OPTIONS)를 전역 허용해 CORS 오류 방지</li>
     * </ul>
     *
     * <p>구성</p>
     * <ul>
     *   <li>OPTIONS preflight 전역 허용</li>
     *   <li>정적 리소스 및 개발 편의(H2 Console) 접근 허용</li>
     *   <li>Swagger, 공통 API, 인증 진입점(Auth) 등 공개 엔드포인트 허용</li>
     *   <li>메서드+경로 조합으로만 공개해야 하는 정책(예: 회원가입 POST) 분리 허용</li>
     * </ul>
     *
     * <p>주의</p>
     * <ul>
     *   <li>이 메서드는 "permitAll"만 다루며, 최종적으로 {@code anyRequest().authenticated()} 같은
     *       기본 정책과 함께 사용되어야 한다.</li>
     *   <li>운영 환경에서는 Swagger/H2 Console 같은 공개 범위를 프로필(dev/stg/prd)로 분리하는 것을 권장한다.</li>
     * </ul>
     */
    private void permitAllEndpoints(
            org.springframework.security.config.annotation.web.configurers
                    .AuthorizeHttpRequestsConfigurer<HttpSecurity>
                    .AuthorizationManagerRequestMatcherRegistry authorize
    ) {
        // Preflight(OPTIONS) 전역 허용: 가장 먼저
        authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

        // 정적 리소스 / H2
        authorize.requestMatchers(toH2Console(), toStaticResources().atCommonLocations()).permitAll();

        // Swagger / Common / Auth
        authorize.requestMatchers(PermitAllPolicy.swaggerPaths()).permitAll();
        authorize.requestMatchers(PermitAllPolicy.commonPaths()).permitAll();
        authorize.requestMatchers(PermitAllPolicy.authPaths()).permitAll();

        // Method + Path 조합
        for (PermitAllPolicy.MethodAndPath mp : PermitAllPolicy.userSignUp()) {
            authorize.requestMatchers(mp.method(), mp.path()).permitAll();
        }
    }

}
