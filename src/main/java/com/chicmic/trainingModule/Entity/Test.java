package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Test {
    @Id
    private String _id;
    private String testName;
    private List<String> teams;
    private List<List<Milestone>> milestones;
    private Boolean deleted = false;

}
