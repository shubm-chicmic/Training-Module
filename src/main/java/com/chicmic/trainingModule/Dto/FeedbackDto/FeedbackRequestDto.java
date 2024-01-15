package com.chicmic.trainingModule.Dto.FeedbackDto;
import com.chicmic.trainingModule.annotation.Conditional;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;

import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.*;

@Getter @Setter
@Conditional(conditionalProperty = "feedbackType", values = {"3"}, requiredProperties = {"course","phase","theoreticalRating","technicalRating","communicationRating","planId"},message = "type-1 error")
@Conditional(conditionalProperty = "feedbackType", values = {"2"}, requiredProperties = {"test","milestone","theoreticalRating","codingRating","communicationRating","planId"},message = "type-2 error")
@Conditional(conditionalProperty = "feedbackType", values = {"4"}, requiredProperties = {"course","communicationRating","technicalRating","presentationRating","planId"},message = "type-3 error")
@Conditional(conditionalProperty = "feedbackType", values = {"5"}, requiredProperties = {"teamSpiritRating","attitudeRating"},message = "type-4 error")
public class FeedbackRequestDto {
    private String _id;

    @NotBlank(message = "Trainee field is required.")
    private String trainee;

    @Pattern(regexp = "^[2-5]$",message = "FeedbackType should be lie b/w 2 to 5")
    @NotBlank(message = "FeedbackType field is required.")
    private String feedbackType;

    private String course;


    private HashSet<String> phase;

    private String test;

    private HashSet<String> milestone;

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

    private String planId;

    private String taskId;
    public Float computeRating(){
        if (feedbackType.equals(VIVA_)) {
            float total = theoreticalRating + technicalRating + communicationRating;
            return total / 3;
        }else if(feedbackType.equals(TEST_)){
            float total = communicationRating + theoreticalRating + codingRating;
            return total/3;
        } else if (feedbackType.equals(PPT_)) {
            float total = communicationRating + technicalRating + presentationRating;
            return total/3;
        }
        float total = teamSpiritRating + attitudeRating;
        return total/2;
    }
}
