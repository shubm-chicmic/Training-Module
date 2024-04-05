package com.chicmic.trainingModule.Controller.MentorController;

import com.chicmic.trainingModule.Config.Security.CustomPermissionEvaluator;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Service.PlanServices.MentorService;
import com.chicmic.trainingModule.Util.Pagenation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/training/mentor")
@AllArgsConstructor
public class MentorView {
    private final MentorService mentorService;
    @RequestMapping(value = {""}, method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM', 'IND', 'TR')")
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
            HttpServletResponse response,
            Principal principal
    ) {
        pageNumber /= pageSize;
        // Get the current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAuthorities = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("TL")
                        || authority.getAuthority().equals("PA")
                        || authority.getAuthority().equals("PM"));
        if(hasAuthorities) {
            return mentorService.getAllMentors(pageNumber, pageSize, sortDirection, sortKey, searchString);
        }
        return mentorService.getMentorOfTrainee(pageNumber, pageSize, sortDirection, sortKey, searchString, principal.getName());
    }

}
