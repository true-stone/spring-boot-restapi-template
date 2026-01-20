package com.example.api.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
@NotBlank
@Size(max = 20)
public @interface ValidUsername {
    String message() default "유효하지 않은 사용자 이름입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
