package com.chicmic.trainingModule.annotation.validator;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.TrainingModuleApplication;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

public class UserValidation implements ConstraintValidator<com.chicmic.trainingModule.annotation.UserValidation, Object> {
    @Override
    public void initialize(com.chicmic.trainingModule.annotation.UserValidation constraintAnnotation) {
        // Initialize validator

    }

    @Override
    public boolean isValid(Object val, ConstraintValidatorContext constraintValidatorContext) {
        if (val == null) {
            return true;
        }
        if (val instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) val;
            for (Object element : collection) {
                if (element instanceof String) {
                    String userId = (String) element;
                    if (!isUserIdPresent(userId)) return false;
                }
                System.out.println("\u001B[43m element = " + element + "\u001B[0m");
            }
            return true;
        }
        return true;
    }

    public boolean isUserIdPresent(String userId) {
        UserDto userDto = TrainingModuleApplication.idUserMap.get(userId);
        if (userDto == null) return false;
        return true;
    }

}
