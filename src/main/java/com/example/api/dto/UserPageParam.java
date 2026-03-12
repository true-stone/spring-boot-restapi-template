package com.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

/**
 * 사용자 목록 조회 페이지네이션 요청 파라미터.
 * <p>
 * 정렬 가능한 필드는 {@link SortField} 에 열거하며,
 * 실제 컬럼명은 {@code property} 필드로 매핑된다.
 * </p>
 */
@Getter
@Setter
public class UserPageParam extends PageParam {

    @Schema(description = "정렬 기준", example = "ID", defaultValue = "ID")
    private SortField sort = SortField.ID;

    public Pageable toPageable() {
        return toPageable(sort.property);
    }

    public enum SortField {
        ID("id");

        private final String property;

        SortField(String property) {
            this.property = property;
        }
    }
}