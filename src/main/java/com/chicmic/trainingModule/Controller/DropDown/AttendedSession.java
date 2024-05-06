package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto.AssignedPlanResponse;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.SessionIdNameAndTypeDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Entity.Constants.TimeSheetType;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
            @RequestParam(value = "entryDate", required = false) String entryDate,
            HttpServletResponse response,
            Principal principal
    )  {
        pageNumber = null;
        pageSize = null;

        System.out.println("Entry date : " + entryDate);
        List<Session> sessionList = sessionService.getAttendedSessions(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
        List<Session> sessionOfTodayDate = new ArrayList<>();
        if (entryDate != null) {
            entryDate = entryDate.trim();
            LocalDate requestedDate = LocalDate.parse(entryDate); // Parse the entryDate parameter to LocalDate
            for (Session session : sessionList) {
                LocalDate sessionDate = session.getDateTime().atZone(ZoneId.systemDefault()).toLocalDate();
                if (sessionDate.equals(requestedDate)) {
                    sessionOfTodayDate.add(session);
                }
            }
        }else {
            sessionOfTodayDate = sessionList;
        }
        List<UserIdAndNameDto> sessionResponseDtoList = sessionResponseMapper.mapSessionToDropdownResponseDto(sessionOfTodayDate);
        SessionIdNameAndTypeDto sessionIdNameAndTypeDto = SessionIdNameAndTypeDto.builder()
                .planType(TimeSheetType.SESSION)
                .sessions(sessionResponseDtoList)
                .build();
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Success", sessionIdNameAndTypeDto));

    }

}
