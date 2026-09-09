package com.food.userinfo.validation.validator;

import com.food.userinfo.validation.OptionalPattern;
import jakarta.validation.ConstraintValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class OptionalPatternValidator implements ConstraintValidator<OptionalPattern,String> {

    private String regexp;

    @Override
    public void initialize(OptionalPattern constraintAnnotation) {
        this.regexp = constraintAnnotation.regexp();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // If value is null or empty, skip validation (optional)
        if (value == null || value.isEmpty()) {
            return true;
        }
        // Validate against regex
        return value.matches(regexp);
    }
}
