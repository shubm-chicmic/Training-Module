package com.chicmic.trainingModule.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DbVersion {
    @Id
    private String _id;
    private int version;
    private LocalDateTime createTimestamp;
    private LocalDateTime updateTimestamp;
}

