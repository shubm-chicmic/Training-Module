package com.chicmic.trainingModule.Controller.MentorController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.FeedbackDto.MentorFeedbackRequestDto;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.FeedBackService.MentorFeedbackService;
import com.chicmic.trainingModule.Service.PlanServices.MentorService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static com.chicmic.trainingModule.Dto.rating.Rating.getRating;
import static com.chicmic.trainingModule.Service.FeedBackService.FeedbackService.compute_rating;

@RestController
@RequestMapping("/v1/training/feedback/")
@AllArgsConstructor
public class MentorRating {
    private final MentorFeedbackService mentorFeedbackService;

    @RequestMapping(value = {"mentor"}, method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('TR')")
    public ApiResponse giveRating(MentorFeedbackRequestDto ratingRequestDto, Principal principal, HttpServletResponse response) {
        if(ratingRequestDto != null) {
            Feedback_V2 feedback = mentorFeedbackService.createFeedbackForMentor(ratingRequestDto, principal.getName());
            return new ApiResponse(HttpStatus.CREATED.value(), "Feedback given to the mentor Successfully", feedback, response);
        }
        return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Invalid Request", null, response);
    }
}
