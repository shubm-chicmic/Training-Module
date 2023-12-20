package com.chicmic.trainingModule.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-check")
public class HealthController {
    @GetMapping
    public String healthCheck(){
        return "Server is up on port 8081";
    }
}
