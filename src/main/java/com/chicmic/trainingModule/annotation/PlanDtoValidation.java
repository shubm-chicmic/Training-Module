package com.chicmic.trainingModule.annotation;

import com.chicmic.trainingModule.annotation.validator.PlanDtoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = PlanDtoValidator.class)
public @interface PlanDtoValidation {
    String message() default "Plan Validation Failed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
