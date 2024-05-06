package com.chicmic.trainingModule.Dto.rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Rating_PPT implements Rating{
    private Double communicationRating;
    private Double technicalRating;
    private Double presentationRating;
    private String courseId;
    public Double computeOverallRating(){
        double total = communicationRating + technicalRating + presentationRating;
        return total/3;
    }
}
