package com.example.api.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공통 API", description = "헬스 체크 등 시스템의 공통 기능을 제공하는 API")
@RestController
@RequestMapping("/common")
public class CommonController {

    @Operation(
            summary = "헬스 체크 (Health Check)",
            description = "API 서버의 현재 상태를 확인합니다. 'OK' 문자열을 반환하면 정상입니다."
    )
    @ApiResponse(responseCode = "200", description = "서버 정상 동작",
            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string", example = "OK")))
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public String healthCheck() {
        return "OK";
    }

    @Hidden
    @GetMapping("/default-handler-ex")
    public String defaultException(@Valid @NotNull @Min(1) @Max(4) @RequestParam Integer data) {
        return "OK";
    }

    @Hidden
    @GetMapping("/default-handler-ex2/{data}")
    public String defaultException2(@PathVariable Integer data) {
        return "OK";
    }
}
