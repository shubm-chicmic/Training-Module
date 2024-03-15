package com.chicmic.trainingModule.annotation.validator;

import com.chicmic.trainingModule.Dto.PlanDto.PlanDto;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.annotation.ApproverValidation;
import com.chicmic.trainingModule.annotation.PlanDtoValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public class PlanDtoValidator implements ConstraintValidator<PlanDtoValidation, PlanDto> {
    @Override
    public boolean isValid(PlanDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return false;
        }
        if(dto.getApproved() != null && dto.getApproved() == true) {
            return true;
        }
        //
        if(dto.getApprover() !=null && dto.getPlanName() == null && dto.getDescription() == null && dto.getPhases() == null){
            return isValidApprovers(dto.getApprover(), context);
        }
        return isValidPlanName(dto.getPlanName(), context)
                && isValidDescription(dto.getDescription(), context)
                && isValidApprovers(dto.getApprover(), context)
                && isValidPhases(dto.getPhases(), context)
                && isValidApproved(dto.getApproved());
    }

    private boolean isValidPlanName(String planName, ConstraintValidatorContext context) {
        if (planName == null || planName.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Plan Name is required")
                    .addConstraintViolation();
            return false;
        } else if (planName.length() < 1 || planName.length() > 40) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Plan Name length must be between 1 and 40 characters")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean isValidDescription(String description, ConstraintValidatorContext context) {
        if (description == null || description.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Description is required")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }


    private boolean isValidApprovers(@ApproverValidation Set<String> approvers, ConstraintValidatorContext context) {
        com.chicmic.trainingModule.annotation.validator.ApproverValidation approverValidator = new com.chicmic.trainingModule.annotation.validator.ApproverValidation();

        if(approvers == null || approvers.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Approver is required")
                    .addConstraintViolation();
            return false;
        }
        else if (!approverValidator.isValid(approvers, context)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Approvers Validation is Failed")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean isValidPhases(List<Phase<PlanTask>> phases, ConstraintValidatorContext context) {
        if(phases == null || phases.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Phases are required")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }


    private boolean isValidApproved(Boolean approved) {
        return true;
    }
}
