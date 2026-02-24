package com.example.api.config;

import com.example.api.annotation.ApiErrorCodeExample;
import com.example.api.dto.ErrorResponse;
import com.example.api.exception.ErrorCode;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("API Documentation")
                .description("Spring Boot API Template Project")
                .version("v1.0.0");

        String jwtSchemeName = "bearerAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // ErrorResponse 클래스를 분석하여 Schema 객체를 생성
        Schema<?> errorResponseSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class))
                .schema;
        Schema<?> fieldErrorSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.FieldError.class))
                .schema;

        Components components = new Components()
                .addSchemas("ErrorResponse", errorResponseSchema)
                .addSchemas("ErrorResponse.FieldError", fieldErrorSchema) // 생성된 스키마를 전역 Components에 등록
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("로그인 후 발급받은 access_token 입력 (bearer 없이)"));

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiErrorCodeExample apiErrorCodeExample = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);

            if (apiErrorCodeExample != null) {
                generateErrorCodeResponse(operation, apiErrorCodeExample.value());
            }
            return operation;
        };
    }

    private void generateErrorCodeResponse(io.swagger.v3.oas.models.Operation operation, ErrorCode[] errorCodes) {
        ApiResponses responses = operation.getResponses();
        Map<Integer, List<ErrorCode>> statusErrorCodeMap = Arrays.stream(errorCodes)
                .collect(Collectors.groupingBy(errorCode -> errorCode.getStatus().value()));

        statusErrorCodeMap.forEach((status, groupedErrorCodes) -> {
            ApiResponse response = new ApiResponse().description(getKoreanDescription(status));

            // MediaType 객체 생성 및 Schema 설정
            MediaType mediaType = new MediaType();
            mediaType.setSchema(new Schema<>().$ref("ErrorResponse"));
            Content content = new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType);
            response.setContent(content);

            groupedErrorCodes.forEach(errorCode -> {
                Example example = createExample(errorCode);
                mediaType.addExamples(errorCode.getCode(), example);
            });

            responses.addApiResponse(String.valueOf(status), response);
        });
    }

    private Example createExample(ErrorCode errorCode) {
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        Example example = new Example();
        example.setSummary(errorCode.getMessage());
        example.setValue(errorResponse);
        return example;
    }

    /**
     * HTTP 상태 코드에 대한 한글 설명을 반환하는 헬퍼 메소드
     */
    private String getKoreanDescription(int statusCode) {
        try {
            HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
            return switch (httpStatus) {
                case BAD_REQUEST -> "잘못된 요청";
                case UNAUTHORIZED -> "인증 실패";
                case FORBIDDEN -> "권한 없음";
                case NOT_FOUND -> "찾을 수 없음";
                case CONFLICT -> "리소스 충돌";
                case INTERNAL_SERVER_ERROR -> "서버 내부 오류";
                default -> httpStatus.getReasonPhrase();
            };
        } catch (IllegalArgumentException e) {
            return "알 수 없는 오류";
        }
    }
}
