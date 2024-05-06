package com.chicmic.trainingModule.Dto;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class SessionIdNameAndTypeDto {
//    private String _id;
//    private String name;
    private Integer planType;
    List<UserIdAndNameDto> sessions = new ArrayList<>();
}
