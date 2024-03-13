package com.chicmic.trainingModule.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = com.chicmic.trainingModule.annotation.validator.ApproverValidation.class)
public @interface ApproverValidation {
    String message() default "Approver Validation Failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
