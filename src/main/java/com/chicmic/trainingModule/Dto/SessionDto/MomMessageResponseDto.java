package com.chicmic.trainingModule.Dto.SessionDto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MomMessageResponseDto {
    private String _id;
    private String name;
    private String message;
}
