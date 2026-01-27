package com.example.api.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static com.example.api.constants.ValidationConstants.*;

/**
 * 사용자 이름(로그인 ID)의 유효성을 검증하기 위한 커스텀 어노테이션.
 * 여러 제약 조건을 조합하여 사용합니다.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
@NotBlank(message = "사용자 이름은 비워둘 수 없습니다.")
@Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH, message = "사용자 이름은 4자 이상 20자 이하여야 합니다.")
@Pattern(
        regexp = USERNAME_PATTERN,
        message = "사용자 이름은 영문 소문자, 숫자, 밑줄(_), 점(.)만 사용할 수 있습니다."
)
public @interface ValidUsername {
    String message() default "유효하지 않은 사용자 이름입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
