package com.example.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI openAPI() {
        // API 정보 설정
        Info info = new Info()
                .title("Template API")
                .description("Template API 문서입니다.")
                .version("1.0.0");

        return new OpenAPI()
                .info(info)
                .components(new Components());
    }
}
