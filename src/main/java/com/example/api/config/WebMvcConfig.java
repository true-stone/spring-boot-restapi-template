package com.example.api.config;

import com.example.api.resolver.CurrentUserArgumentResolver;
import com.example.api.resolver.CurrentUserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    /**
     * 콘텐츠 협상(Content Negotiation)을 전략을 설정합니다.
     * 모든 API 응답의 기본 미디어 타입을 application/json 으로 설정합니다.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                // URL 경로의 확장자(예: .json, .xml)로 미디어 타입을 결정하는 기능을 비활성화합니다.
                .favorParameter(false)
                // 요청의 'Accept' 헤더를 무시하고, 항상 기본 설정된 미디어 타입으로 응답하도록 설정합니다.
                .ignoreAcceptHeader(false)
                // 기본 미디어 타입을 application/json으로 설정합니다.
                .defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")                            // 허용할 출처
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*")                                                // 모든 헤더 허용
                .allowCredentials(true)                                             // 쿠키/인증 정보 포함 허용
                .maxAge(3600);                                                      // pre-flight 요청의 캐시 시간 (초)
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
        resolvers.add(currentUserIdArgumentResolver);
    }

}