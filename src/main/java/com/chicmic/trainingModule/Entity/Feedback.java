package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.ratings.Rating;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter @Setter @Builder
@Document
public class Feedback {
    @Id
    private String id;
    private String traineeID;
    private String type;
    private Rating rating;
    private String comment;
    private Date createdAt;
    private Date updateAt;
    private String createdBy;
    private Float overallRating;
}
