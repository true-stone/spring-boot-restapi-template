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
@Size(min = 4, max = 20)
@Pattern(
        regexp = "^[a-zA-Z0-9_.]+$",
        message = "username은 영문/숫자/._ 만 허용하며 공백은 허용되지 않습니다."
)
public @interface ValidUsername {
    String message() default "유효하지 않은 사용자 이름입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
