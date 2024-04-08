package com.chicmic.trainingModule.Dto.FeedbackDto;

import com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MentorFeedbackRequestDto {
    @NotBlank(message = "Mentor is required.")
    private String mentor;
    private String feedbackType = FeedbackType.MENTOR_;
    @DecimalMin(value = "0.5", message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5, message = "Rating should be less than or equal to 5")
    private Double communicationRating;
    @DecimalMin(value = "0.5", message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5, message = "Rating should be less than or equal to 5")
    private Double technicalRating;
    @DecimalMin(value = "0.5", message = "Rating should be greater than or equal to 0.5")
    @Max(value = 5, message = "Rating should be less than or equal to 5")
    private Double attitudeRating;
    @NotBlank(message = "Comment field is required.")
    private String comment;

    public Double computeRating() {
        double total = communicationRating + technicalRating + attitudeRating;
        return total / 3;
    }
}
