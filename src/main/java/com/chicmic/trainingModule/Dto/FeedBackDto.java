package com.chicmic.trainingModule.Dto;

import com.chicmic.trainingModule.annotation.Conditional;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Conditional(selected = "feedBackTypeId", values = {"1"}, required = {"courseId","phaseId","theoreticalRating","technicalRating","communicationRating"})
@Conditional(selected = "feedBackTypeId", values = {"2"}, required = {"testId","milestoneId","theoreticalRating","codingRating","communicationRating"})
@Conditional(selected = "feedBackTypeId", values = {"3"}, required = {"communicationRating","technicalRating","presentationRating"})
@Conditional(selected = "feedBackTypeId", values = {"4"}, required = {"teamSpiritRating","attitudeRating"})
public class FeedBackDto {
    private String _id;
    @NotBlank(message = "Trainee field is required.")
    private String traineeId;

    @Pattern(regexp = "^[1-4]$",message = "FeebackType should be lie b/w 1 to 4")
    @NotBlank(message = "FeedbackType field is required.")
    private String feedBackTypeId;

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

    @NotBlank(message = "Message field is required.")
    private String message;
}
