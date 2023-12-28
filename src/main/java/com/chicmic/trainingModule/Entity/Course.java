package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.annotation.CascadeSave;
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
public class Course {
    @Id
    private ObjectId _id;
    private String name;
    private String figmaLink;
    private String guidelines;
    @DBRef
    @CascadeSave
    private List<Phase> phases;
    private Set<String> approver = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
//    public Course() {
//        Phase.count = 0;
//    }
}
