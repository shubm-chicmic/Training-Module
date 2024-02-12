package com.chicmic.trainingModule.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/training/serverStatus")
public class HealthController {
    @GetMapping
    public String healthCheck(HttpServletRequest request){
        return "Server is up on port " + request.getServerPort();
    }
}
