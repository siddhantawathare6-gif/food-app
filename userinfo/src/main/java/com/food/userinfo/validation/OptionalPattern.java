package com.food.userinfo.validation;

import com.food.userinfo.validation.validator.OptionalPatternValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OptionalPatternValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalPattern {
    String regexp();
    String message() default "Invalid format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
