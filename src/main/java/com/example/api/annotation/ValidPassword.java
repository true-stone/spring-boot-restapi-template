package com.example.api.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static com.example.api.constants.ValidationConstants.*;

/**
 * 사용자 암호(패스워드)의 유효성을 검증하기 위한 커스텀 어노테이션.
 * 여러 제약 조건을 조합하여 사용합니다.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
@NotBlank
@Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
@Pattern(
        regexp = PASSWORD_PATTERN,
        message = "비밀번호는 영문 소문자, 대문자, 숫자, 특수문자를 포함해야 합니다."
)
public @interface ValidPassword {
    String message() default "유효하지 않은 비밀번호입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
