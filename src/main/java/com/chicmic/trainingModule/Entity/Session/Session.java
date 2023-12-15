package com.chicmic.trainingModule.Entity.Session;

import com.chicmic.trainingModule.Entity.StatusConstants;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
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
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private String dateTime;
    private int status = StatusConstants.PENDING;
    private boolean isDeleted = false;
    private boolean isApproved = false;
    private List<MomMessage> MOM = new ArrayList<>();

}
