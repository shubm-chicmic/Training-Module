package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Util.ConversionUtility;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedPlan {
    @Id
    private String _id;
    private String userId;
    private LocalDateTime date;
    @DBRef
    private List<Plan> plans;
    private Set<String> reviewers = new HashSet<>();
    private String createdBy;
    private Boolean deleted = false;
    private Boolean approved = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public List<UserIdAndNameDto> getReviewerDetails() {
        return ConversionUtility.convertToUserIdAndName(this.reviewers);
    }

}
