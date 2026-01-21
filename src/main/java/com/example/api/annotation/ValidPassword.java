package com.example.api.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
@NotBlank
@Size(min = 8, max = 64)
@Pattern(
        regexp = "^(?!.*\\s).+$",
        message = "password에는 공백을 포함할 수 없습니다."
)
public @interface ValidPassword {
    String message() default "유효하지 않은 비밀번호입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
