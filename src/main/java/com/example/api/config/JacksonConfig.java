package com.example.api.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // Jackson 3는 기본이 ISO-8601 문자열이지만, 의도를 명확히 해두면 안전
            builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.defaultTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            // unknown field 무시
            builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            // 빈 직렬화 예외 방지 - 프록시/빈 구조 때문에 “빈 객체” 직렬화 시 예외가 나는 케이스 방지
            builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            // enum 대소문자 허용
            builder.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

            // 선택
            // builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        };
    }
}
