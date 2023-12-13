package com.chicmic.trainingModule.Dto;

import com.chicmic.trainingModule.annotation.Conditional;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Conditional(conditionalProperty = "feedbackType", values = {"1"}, requiredProperties = {"courseId","phaseId","theoreticalRating","technicalRating","communicationRating"},message = "type-1 error")
@Conditional(conditionalProperty = "feedbackType", values = {"2"}, requiredProperties = {"testId","milestoneId","theoreticalRating","codingRating","communicationRating"},message = "type-2 error")
@Conditional(conditionalProperty = "feedbackType", values = {"3"}, requiredProperties = {"communicationRating","technicalRating","presentationRating"},message = "type-3 error")
@Conditional(conditionalProperty = "feedbackType", values = {"4"}, requiredProperties = {"teamSpiritRating","attitudeRating"},message = "type-4 error")
public class FeedBackDto {
    private String _id;

    @NotBlank(message = "Trainee field is required.")
    private String trainee;

    @Pattern(regexp = "^[1-4]$",message = "FeebackType should be lie b/w 1 to 4")
    @NotBlank(message = "FeedbackType field is required.")
    private String feedbackType;

    private String courseId;


    private String phaseId;

    private String testId;

    private String milestoneId;

    @DecimalMin(value="0.5",message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5,message = "Rating should be less than or equal to 5")
    private Float communicationRating;

    @DecimalMin(value="0.5",message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5,message = "Rating should be less than or equal to 5")
    private Float presentationRating;

    @DecimalMin(value="0.5",message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5,message = "Rating should be less than or equal to 5")
    private Float technicalRating;

    @DecimalMin(value="0.5",message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5,message = "Rating should be less than or equal to 5")
    private Float theoreticalRating;

    @DecimalMin(value="0.5",message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5,message = "Rating should be less than or equal to 5")
    private Float codingRating;

    @DecimalMin(value="0.5",message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5,message = "Rating should be less than or equal to 5")
    private Float attitudeRating;

    @DecimalMin(value="0.5",message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5,message = "Rating should be less than or equal to 5")
    private Float teamSpiritRating;

    @NotBlank(message = "Comment field is required.")
    private String comment;
}
