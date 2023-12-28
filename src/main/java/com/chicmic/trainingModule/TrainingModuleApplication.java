package com.chicmic.trainingModule;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
public class TrainingModuleApplication {
	private static final String excelFileName = "Unreal Learning schedule.xlsx";
	public static final Map<String, UserDto> idUserMap = new HashMap<>();
	public static final Map<String, String> teamIdAndNameMap = new HashMap<>();
	public static HashMap<Integer, String> zoneCategoryMap = new HashMap<>();
	private static void findUsersAndMap() throws JsonProcessingException {
		String apiUrl = "https://timedragon.staging.chicmic.co.in/v1/dropdown/user";
		RestTemplate restTemplate = new RestTemplate();
		String apiResponse = restTemplate.getForObject(apiUrl, String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseNode = mapper.readTree(apiResponse);
		JsonNode dataArray = responseNode.get("data");

		for (JsonNode node : dataArray) {
			UserDto userDto = UserDto.builder()
					.token(null)
					._id(node.get("_id").asText())
					.name(node.get("name").asText())
					.empCode(node.get("employeeId").asText())
					.teamId((node.get("teams").asText()))
					.teamName((node.get("teamNames").asText()))
					.build();
			idUserMap.put(userDto.get_id(), userDto);
		}
	}
	private static void findTeamsAndMap() throws JsonProcessingException {
		String apiUrl = "https://timedragon.staging.chicmic.co.in/v1/dropdown/team";
		RestTemplate restTemplate = new RestTemplate();
		String apiResponse = restTemplate.getForObject(apiUrl, String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseNode = mapper.readTree(apiResponse);
		JsonNode dataArray = responseNode.get("data");

		for (JsonNode node : dataArray) {
			String userId = node.get("_id").asText();
			String userName = node.get("name").asText();
			teamIdAndNameMap.put(userId, userName);
		}
	}

	//fetching trainee list
	public static HashMap<String, UserDto> findTraineeAndMap(){
		String apiUrl = "https://timedragon.staging.chicmic.co.in/v1/dropdown/user?designation=Trainee";
		RestTemplate restTemplate = new RestTemplate();
		String apiResponse = restTemplate.getForObject(apiUrl, String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseNode = null;
		try {
			responseNode = mapper.readTree(apiResponse);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		JsonNode dataArray = responseNode.get("data").get("data");

		HashMap<String,UserDto> idTraineeMap = new HashMap<>();
		for (JsonNode node : dataArray) {
			UserDto userDto = UserDto.builder()
					.token(null)
					._id(node.get("_id").asText())
					.name(node.get("name").asText())
					.empCode(node.get("employeeId").asText())
					.teamId((node.get("teams").get(0).get("_id").asText()))
					.teamName((node.get("teams").get(0).get("name").asText()))
					.build();
			idTraineeMap.put(userDto.get_id(), userDto);
		}
		return idTraineeMap;
	}

	public static String searchNameById(String userId) {
		UserDto userDto = idUserMap.get(userId);
		if(userId == null || userId.isEmpty() || userDto == null) {
			return "User not found";
		}
		return userDto.getName();
	}

	public static UserDto searchUserById(String userId){
		UserDto userDto = idUserMap.get(userId);
		if(userId == null || userId.isEmpty() || userDto == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid userId");
		}
		return userDto;
	}

	public static String searchTeamById(String id) {
		return teamIdAndNameMap.getOrDefault(id, "User not found");
	}

	public static void main(String[] args) throws JsonProcessingException {
		findUsersAndMap();
		findTeamsAndMap();
		zoneCategoryMap.put(1, "1st Zone");
		zoneCategoryMap.put(2, "2nd Main Zone");
		zoneCategoryMap.put(3, "2nd Side Zone");
		zoneCategoryMap.put(4, "3rd Main Zone");
		zoneCategoryMap.put(5, "3rd Side Zone");
		zoneCategoryMap.put(6, "4th Zone");
		System.out.println("\u001B[31m VAlue =================== " + zoneCategoryMap.get(4) + "\u001B[0m");
		SpringApplication.run(TrainingModuleApplication.class, args);
//		ExcelPerformOperations.excelPerformOperations(excelFileName);
	}

	@Bean
	public static BCryptPasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder;
	}

	@Bean
	public static ThreadPoolTaskExecutor taskExecutor(){
		ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(0);
		executor.setMaxPoolSize(100);
		executor.setQueueCapacity(200);
		executor.setThreadNamePrefix("cached-thread-");
		executor.setAllowCoreThreadTimeOut(true);
		executor.initialize();
		return executor;
	}


	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
