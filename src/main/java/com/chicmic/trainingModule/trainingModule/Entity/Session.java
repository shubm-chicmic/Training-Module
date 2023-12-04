package com.chicmic.trainingModule.trainingModule.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "session")
public class Session {
    @Id
    private Long id;
    private String title;
    private String time;
    private String date;
    private String location;
    private List<Object> teams;
    private List<Object> trainings;
    private List<Object> sessionsBy;

    private String status;
    private boolean isDeleted = false;
    private String MOM;

}
