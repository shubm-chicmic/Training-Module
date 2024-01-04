package com.chicmic.trainingModule.Util;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static com.chicmic.trainingModule.TrainingModuleApplication.idUserMap;

public class FeedbackUtil {
    public static  final String[] FEEDBACK_TYPE_CATEGORY =
            {"COURSE","TEST","PPT","BEHAVIOUR"};
    public static  final String[] FEEDBACK_TYPE_CATEGORY_V2 =
            {"COURSE","TEST","BEHAVIOUR","PPT"};
    public static final Map<String, Integer> FEEDBACKS_V2 = new HashMap<String, Integer>() {{
        put("COURSE", 1);
        put("TEST", 2);
        put("PPT", 4);
        put("BEHAVIOUR",3);
        // Add more predefined key-value pairs as needed
    }};

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
    public static boolean checkRole(String roleName){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().contains(roleName);
    }
    public static String getFeedbackMessageBasedOnOverallRating(Float overallRating){
        if(overallRating==0) return "";
        else if(overallRating < 1 && overallRating > 0)
            return  "Considerable Work Required.";
        else if (overallRating < 2)
            return "Needs Significant Enhancement.";
        else if (overallRating < 3)
            return "Room for Improvement.";
        else if (overallRating < 4) {
            return "Almost There!";
        }
        return "Impressive! Well Done!";
    }
}
