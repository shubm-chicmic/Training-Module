package com.chicmic.trainingModule.Util;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.TrainingModuleApplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.chicmic.trainingModule.TrainingModuleApplication.idUserMap;

public class FeedbackUtil {
    public static  final String[] FEEDBACK_TYPE_CATEGORY =
            {"COURSE","TEST","PPT","BEHAVIOUR"};

    public static HashSet<String> searchNameAndEmployeeCode(String query){
        HashSet<String> ids = new HashSet<>();
        String searchQuery = query.toLowerCase();
        //idUserMap
        for (Map.Entry entry : idUserMap.entrySet()){
            //searching text
            UserDto userDto = (UserDto) entry.getValue();
            if(userDto.getName().toLowerCase().contains(searchQuery) || userDto.getTeamName().toLowerCase().contains(searchQuery)){
                ids.add(userDto.get_id());
            }
        }
        return ids;
    }
}
