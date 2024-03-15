//package com.chicmic.trainingModule.annotation.validator;
//
//import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
//import com.chicmic.trainingModule.Entity.Phase;
//import com.chicmic.trainingModule.Entity.Task;
//import com.chicmic.trainingModule.annotation.ApproverValidation;
//import jakarta.validation.ConstraintValidatorContext;
//
//import java.util.List;
//import java.util.Set;
//
//public class CourseDtoValidator implements ConstraintValidator<CourseDtoValidation, CourseDto> {
//    @Override
//    public boolean isValid(CourseDto courseDto, ConstraintValidatorContext context) {
//        if (courseDto == null) {
//            return false;
//        }
//        if(courseDto.getApproved() != null && courseDto.getApproved() == true) {
//            return true;
//        }
//        if(courseDto.getApprover() !=null && courseDto.getName() == null && courseDto.getFigmaLink() == null && courseDto.getPhases() == null){
//            return isValidApprovers(courseDto.getApprover(), context);
//        }
//
//        return isValidPlanName(courseDto.getName(), context)
//                && isValidDescription(courseDto.getFigmaLink(), context)
//                && isValidApprovers(courseDto.getApprover(), context)
//                && isValidPhases(courseDto.getPhases(), context)
//                && isValidApproved(courseDto.getApproved());
//    }
//
//    private boolean isValidPlanName(String planName, ConstraintValidatorContext context) {
//        if (planName == null || planName.isEmpty()) {
//            context.disableDefaultConstraintViolation();
//            context.buildConstraintViolationWithTemplate("Plan Name is required")
//                    .addConstraintViolation();
//            return false;
//        } else if (planName.length() < 1 || planName.length() > 40) {
//            context.disableDefaultConstraintViolation();
//            context.buildConstraintViolationWithTemplate("Plan Name length must be between 1 and 40 characters")
//                    .addConstraintViolation();
//            return false;
//        }
//        return true;
//    }
//
//    private boolean isValidDescription(String description, ConstraintValidatorContext context) {
//        if (description == null || description.isEmpty()) {
//            context.disableDefaultConstraintViolation();
//            context.buildConstraintViolationWithTemplate("Description is required")
//                    .addConstraintViolation();
//            return false;
//        }
//        return true;
//    }
//
//
//    private boolean isValidApprovers(@ApproverValidation Set<String> approvers, ConstraintValidatorContext context) {
//        com.chicmic.trainingModule.annotation.validator.ApproverValidation approverValidator = new com.chicmic.trainingModule.annotation.validator.ApproverValidation();
//
//        if(approvers == null || approvers.isEmpty()) {
//            context.disableDefaultConstraintViolation();
//            context.buildConstraintViolationWithTemplate("Approver is required")
//                    .addConstraintViolation();
//            return false;
//        }
//        else if (!approverValidator.isValid(approvers, context)){
//            context.disableDefaultConstraintViolation();
//            context.buildConstraintViolationWithTemplate("Approvers Validation is Failed")
//                    .addConstraintViolation();
//            return false;
//        }
//        return true;
//    }
//
//    private boolean isValidPhases(List<Phase<Task>> phases, ConstraintValidatorContext context) {
//        if(phases == null || phases.isEmpty()) {
//            context.disableDefaultConstraintViolation();
//            context.buildConstraintViolationWithTemplate("Phases are required")
//                    .addConstraintViolation();
//            return false;
//        }
//        return true;
//    }
//
//
//    private boolean isValidApproved(Boolean approved) {
//        return true;
//    }
//}
