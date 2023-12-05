package com.chicmic.trainingModule.Entity;

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
    private String _id;
    private String title;
    private List<Object> teams;
    private List<Object> trainees;
    private List<Object> sessionBy;
    private String location;
    private List<Object> approver;
    private String time;
    private String status;
    private boolean isDeleted = false;
    private boolean isApproved = false;
    private String MOM;

}
