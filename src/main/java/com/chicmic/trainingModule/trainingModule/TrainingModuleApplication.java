package com.chicmic.trainingModule.trainingModule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class TrainingModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingModuleApplication.class, args);
	}

}
