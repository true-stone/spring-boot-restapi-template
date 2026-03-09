package com.example.api.config;

import com.example.api.filter.HttpLoggingFilter;
import com.example.api.filter.JwtAuthenticationFilter;
import com.example.api.filter.LoggingFilter;
import com.example.api.jwt.JwtAccessDeniedHandler;
import com.example.api.jwt.JwtAuthenticationEntryPoint;
import com.example.api.security.PermitAllPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toStaticResources;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HttpLoggingFilter httpLoggingFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * Spring Security 필터 체인을 완전히 무시할 경로를 설정합니다.
     * (정적 리소스, 개발용 콘솔 등 보안이 전혀 필요 없는 경로)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toH2Console())
                .requestMatchers(toStaticResources().atCommonLocations())
                .requestMatchers(PermitAllPolicy.swaggerPaths());
    }

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
                    permitAllEndpoints(authorize);
                    authorize.anyRequest().authenticated();
                })

                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                .addFilterBefore(httpLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, HttpLoggingFilter.class)
        ;


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private void permitAllEndpoints(
            org.springframework.security.config.annotation.web.configurers
                    .AuthorizeHttpRequestsConfigurer<HttpSecurity>
                    .AuthorizationManagerRequestMatcherRegistry authorize
    ) {
        authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
        authorize.requestMatchers(PermitAllPolicy.commonPaths()).permitAll();
        authorize.requestMatchers(PermitAllPolicy.authPaths()).permitAll();

        // Method + Path 조합
        for (PermitAllPolicy.MethodAndPath mp : PermitAllPolicy.userSignUp()) {
            authorize.requestMatchers(mp.method(), mp.path()).permitAll();
        }
    }
}
