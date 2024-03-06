package com.chicmic.trainingModule.validator;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.TrainingModuleApplication;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

public class ApproverValidation implements ConstraintValidator<com.chicmic.trainingModule.annotation.ApproverValidation, Object> {
    @Override
    public void initialize(com.chicmic.trainingModule.annotation.ApproverValidation constraintAnnotation) {
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
                    if (!isUserIdPresentInApproverDropdownApiResponse(userId)) return false;
                }
                System.out.println("\u001B[43m element = " + element + "\u001B[0m");
            }
            return true;
        }
        return true;
    }

    public boolean isUserIdPresentInApproverDropdownApiResponse(String userId) {
        String apiUrl = TrainingModuleApplication.apiGateWayUrl + "/v1/dropdown/role/user";
        String authToken = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        String employeeIdToMatch = userId;

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request body
        String requestBody = "{\"roles\":[\"PA\",\"PM\",\"TL\",\"HRM\"]}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Make the POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        // Process response
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        String responseBody = responseEntity.getBody();

        System.out.println("Response status code: " + statusCode);

        // Check if any ID matches the employee ID
        if (responseBody != null && responseBody.contains(employeeIdToMatch)) {
            System.out.println("Employee ID matched in the response!");
            return true;
        }
        return false;
    }

}
