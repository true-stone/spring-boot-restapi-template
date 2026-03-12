package com.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 페이지네이션 공통 요청 파라미터 베이스 클래스.
 * <p>
 * JPA: {@code toPageable(sortProperty)} 또는 서브클래스의 {@code toPageable()} 사용<br>
 * MyBatis: {@code getPage()}, {@code getSize()}, {@code getDirection()} 직접 사용
 * </p>
 */
@Getter
@Setter
public abstract class PageParam {

    @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(1)
    private int page = 1;

    @Schema(description = "페이지 당 데이터 수", example = "20", defaultValue = "20", minimum = "1", maximum = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(1)
    @Max(100)
    private int size = 20;

    @Schema(description = "정렬 방향", example = "DESC", defaultValue = "DESC")
    private Direction direction = Direction.DESC;

    public enum Direction {
        ASC, DESC;

        public Sort.Direction toSpring() {
            return Sort.Direction.valueOf(this.name());
        }
    }

    /**
     * 서브클래스에서 정렬 기준 필드를 지정해 Spring Pageable 로 변환한다.
     */
    protected Pageable toPageable(String sortProperty) {
        return PageRequest.of(page - 1, size, direction.toSpring(), sortProperty);
    }
}