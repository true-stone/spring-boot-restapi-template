package com.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "페이지네이션 응답 DTO")
public record PageResponse<T>(

        @Schema(description = "현재 페이지 데이터 목록")
        List<T> content,

        @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
        int page,

        @Schema(description = "페이지 당 데이터 수", example = "20")
        int size,

        @Schema(description = "전체 데이터 수", example = "100")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "첫 페이지 여부", example = "true")
        boolean first,

        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
