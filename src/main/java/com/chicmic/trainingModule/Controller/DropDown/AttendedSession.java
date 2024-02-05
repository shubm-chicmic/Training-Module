package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto.AssignedPlanResponse;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Service.SessionService.SessionResponseMapper;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/training/dropdown")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM','IND', 'TR')")
public class AttendedSession {
    private final SessionService sessionService;
    private final SessionResponseMapper sessionResponseMapper;
    @RequestMapping(value = {"/attendedSession"}, method = RequestMethod.GET)
    public ResponseEntity<ApiResponse> getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "title", required = false) String sortKey,
            HttpServletResponse response,
            Principal principal
    )  {
        pageNumber = null;
        pageSize = null;

        List<Session> sessionList = sessionService.getAttendedSessions(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
        List<SessionResponseDto> sessionResponseDtoList = sessionResponseMapper.mapSessionToDropdownResponseDto(sessionList);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Success", sessionResponseDtoList));

    }

}
