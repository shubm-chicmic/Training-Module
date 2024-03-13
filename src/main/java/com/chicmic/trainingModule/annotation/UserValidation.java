package com.chicmic.trainingModule.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = com.chicmic.trainingModule.annotation.validator.UserValidation.class)
public @interface UserValidation {
    String message() default "User Validation Failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
