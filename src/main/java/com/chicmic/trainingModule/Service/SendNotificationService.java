package com.chicmic.trainingModule.Service;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Service
public class SendNotificationService {
    private final RestTemplate restTemplate;

    public SendNotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void sendNotification() {
        String url = "https://timedragon.staging.chicmic.co.in/v1/notification";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MWZiYTg0ZWY0ZjcwZDZjMGIzZWZmYTkiLCJlbWFpbCI6Im1zcml2YXN0YXYxMThAZ21haWwuY29tIiwidGltZSI6MTY5ODM4OTA3MzU1NCwiaWF0IjoxNjk4Mzg5MDczfQ.kgMBRlQ0m_N99fKi632ZWZ2im3UBfDg4jcz9z5PvxHs");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", "Hello World");
        requestBody.put("notificationType", 3);
        requestBody.put("message", "Plan Updated Successfully");
        requestBody.put("moduleId", "fasfhjasbfjasbf");
        requestBody.put("userIds", Arrays.asList("64ae8db122106e9847bd10a2", "64e2e98aecc13d506c72c73a"));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Object> result = restTemplate.postForEntity(url, requestEntity, Object.class);
    }
}
