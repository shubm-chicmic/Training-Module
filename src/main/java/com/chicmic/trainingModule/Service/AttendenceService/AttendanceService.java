package com.chicmic.trainingModule.Service.AttendenceService;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final RestTemplate restTemplate;
    @Value("${apiGatewayUrl}")
    private String apiGatewayUrl;

    public double getAttendanceRating(String userId, String token) {
        if (userId == null || userId.isEmpty()) {
            return 0.0f;
        }
        UserDto userDto = TrainingModuleApplication.searchUserById(userId);
        if (userDto == null) {
            return 0.0f;
        }
        Instant joiningDate = userDto.getJoiningDate();
        String workingAt = userDto.getWorkingAt();
        Integer totalWorkingDays = calculateTotalWorkingDays(joiningDate, token, workingAt);
        System.out.println("Total Working Days : " + totalWorkingDays);
        Integer totalLeaves = calculateTotalLeaves(userId, token);
        System.out.println("Total Leaves : " + totalLeaves);
        // Calculate attendance percentage based on totalWorkingDays and totalLeaves
        return calculateAttendanceRating(totalWorkingDays, totalLeaves);
    }

    private Integer calculateTotalLeaves(String userId, String token) {
        YearMonth currentYearMonth = YearMonth.now();
        int year = currentYearMonth.getYear();
        int month = currentYearMonth.getMonthValue();

        // Build the URL for the API endpoint
        String apiUrl = apiGatewayUrl + "/v1/user/attendance?userId=" + userId + "&year=" + year + "&month=" + month;

        try {
            // Set the token in the header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the HTTP GET request with the token in the header
            ResponseEntity<Map> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

            // Extract the leave count from the response
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> response = responseEntity.getBody();
//                System.out.println("Leave response: " + response);
                if (response != null && response.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    if (data != null && data.containsKey("yearlyLeavesTaken")) {
                        return (int) data.get("yearlyLeavesTaken");
                    } else {
                        // Handle if the response data does not contain yearlyLeavesTaken
                        System.err.println("Error: No yearlyLeavesTaken found in the response data");
                    }
                } else {
                    // Handle if the response does not contain data
                    System.err.println("Error: No data found in the response");
                }
            } else {
                // Handle non-2xx status codes
                System.err.println("Error: HTTP request failed with status code " + responseEntity.getStatusCodeValue());
            }
        } catch (Exception e) {
            // Handle HTTP request errors
            System.err.println("Error occurred while fetching attendance data: " + e.getMessage());
            e.printStackTrace();
        }

        // Return 0 if there is an error or yearlyLeavesTaken is not available
        return 0;
    }


    private Integer calculateTotalWorkingDays(Instant joiningDate, String token, String workingAt) {
        int year = LocalDate.ofInstant(joiningDate, ZoneOffset.UTC).getYear();
        Set<LocalDate> holidays = fetchHolidays(year, token, workingAt);
        LocalDate startDate = LocalDate.ofInstant(joiningDate, ZoneOffset.UTC);
        LocalDate endDate = LocalDate.now(ZoneOffset.UTC);

        int totalWorkingDays = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (isWorkingDay(date) && !holidays.contains(date)) {
                totalWorkingDays++;
            }
            date = date.plusDays(1);
        }
        return totalWorkingDays;
    }

    private Set<LocalDate> fetchHolidays(int year, String token, String workingAt) {
        Set<LocalDate> holidays = new HashSet<>();
        String apiUrl = apiGatewayUrl + "/v1/holiday?year=" + year + "&company=" + workingAt;
        System.out.println("API URL : " + apiUrl);
        HttpHeaders headers = new HttpHeaders();
//        System.out.println("Token Value : " + token + "Working At : " + workingAt);
        headers.set("Authorization", token);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                try {
                    JsonNode responseNode = new ObjectMapper().readTree(responseEntity.getBody());
                    JsonNode dataArray = responseNode.get("data");

                    if (dataArray != null && dataArray.isArray()) {
                        for (JsonNode holidayNode : dataArray) {
                            JsonNode isDeletedNode = holidayNode.get("isDeleted");
                            if (isDeletedNode != null && !isDeletedNode.asBoolean()) {
                                String dateString = holidayNode.get("date").asText().split("T")[0];
                                LocalDate holidayDate = LocalDate.parse(dateString);
                                holidays.add(holidayDate);
                            }
                        }
                    } else {
                        System.out.println("Data array is not present or is not an array.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle error
                }
            } else {
                // Handle non-200 response
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error
            System.out.println("Failed to fetch holidays. Server error: " + e.getMessage());
            return holidays;
        }

        return holidays;
    }

    private boolean isWorkingDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    private Float calculateAttendanceRating(int totalWorkingDays, int totalLeaves) {
        // Implementation to calculate attendance percentage
        float attendancePercentage = ((totalWorkingDays - totalLeaves) * 5.0f / totalWorkingDays);
        return attendancePercentage;
    }
}
