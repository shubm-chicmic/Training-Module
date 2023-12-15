package com.chicmic.trainingModule.Entity.Plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Plan {
    @Id
    private String _id;
    private String name;
    private String description;


}
