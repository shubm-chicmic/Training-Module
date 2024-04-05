package com.chicmic.trainingModule.Controller.MentorController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Service.PlanServices.MentorService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/feedback/")
@AllArgsConstructor
public class MentorRating {
    private final MentorService mentorService;
    @RequestMapping(value = {"mentor"}, method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('TR')")
    public ApiResponse giveRating(Principal principal) {
        return null;
    }
}
