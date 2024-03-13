package com.chicmic.trainingModule.annotation.validator;

import com.chicmic.trainingModule.annotation.Conditional;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.apache.commons.beanutils.BeanUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
public class ConditionalValidator implements ConstraintValidator<Conditional, Object> {

//    private static final Logger log = LoggerFactory.getLogger(ConditionalValidator.class);

    private String conditionalProperty;
    private String[] requiredProperties;
    private String message;
    private String[] values;

    @Override
    public void initialize(Conditional constraint) {
        conditionalProperty = constraint.conditionalProperty();
        requiredProperties = constraint.requiredProperties();
        message = constraint.message();
        values = constraint.values();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        try {
            Object conditionalPropertyValue = BeanUtils.getProperty(object, conditionalProperty);
            if (doConditionalValidation(conditionalPropertyValue)) {
                return validateRequiredProperties(object, context);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            return false;
        }
        return true;
    }

    private boolean validateRequiredProperties(Object object, ConstraintValidatorContext context) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        boolean isValid = true;
        for (String property : requiredProperties) {
            Object requiredValue = BeanUtils.getProperty(object, property);
            boolean isPresent = requiredValue != null && !isEmpty(requiredValue);
            if (!isPresent) {
                isValid = false;
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate(String.format("%s field is required.", property))
                        .addPropertyNode(property)
                        .addConstraintViolation();
            }
        }
        return isValid;
    }

    private boolean doConditionalValidation(Object actualValue) {
        return Arrays.asList(values).contains(actualValue);
    }
}