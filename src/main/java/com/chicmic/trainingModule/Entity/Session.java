package com.chicmic.trainingModule.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private List<String> teams;
    private List<String> trainees;
    private List<String> sessionBy;
    private String location;
    private List<String> approver;
    private List<String> approvedBy = new ArrayList<>();
    private String createdBy;
    private String time;
    private int status = 1;
    private boolean isDeleted = false;
    private boolean isApproved = false;
    private String MOM;

}
