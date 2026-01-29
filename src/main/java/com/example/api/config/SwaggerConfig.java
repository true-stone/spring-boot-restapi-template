package com.example.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI openAPI() {
        // API 정보 설정
        Info info = new Info()
                .title("Spring Boot API")
                .description("Spring Boot API 문서입니다.")
                .version("v0.0.1");

        Components components = new Components()
                .addSecuritySchemes(BEARER_AUTH, securityScheme());

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement())
                .components(components);
    }

    @Bean
    SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("로그인 후 발급받은 access_token 입력 (bearer 없이)");
    }

    @Bean
    public SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList(BEARER_AUTH);
    }
}
