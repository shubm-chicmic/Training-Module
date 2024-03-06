package com.chicmic.trainingModule.Util;


import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.TrainingModuleApplication;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConversionUtility {
    public static List<UserIdAndNameDto> convertToUserIdAndName(Collection<String> ids) {
        return Optional.ofNullable(ids)
                .map(idCollection -> idCollection.stream()
                        .map(id -> {
                            String name = TrainingModuleApplication.searchNameById(id);
                            return new UserIdAndNameDto(id, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);
    }

    public static List<UserIdAndNameDto> convertToTeamIdAndName(Collection<String> ids) {
        return Optional.ofNullable(ids)
                .map(idCollection -> idCollection.stream()
                        .map(id -> {
                            String name = TrainingModuleApplication.searchTeamById(id);
                            return new UserIdAndNameDto(id, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);
    }
}
