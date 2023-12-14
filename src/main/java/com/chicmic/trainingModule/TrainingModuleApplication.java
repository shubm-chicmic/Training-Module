package com.chicmic.trainingModule;

import com.chicmic.trainingModule.Dto.UserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootApplication
public class TrainingModuleApplication {
	private static final String excelFileName = "Unreal Learning schedule.xlsx";
	public static final Map<String, UserDto> idUserMap = new HashMap<>();
	public static final Map<String, String> teamIdAndNameMap = new HashMap<>();
	public static final Map<String, UserDto> traineeIdAndUserMap = new HashMap<>();
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
	private static void findTraineeAndMap() throws JsonProcessingException {
//		String apiUrl = "https://timedragon.staging.chicmic.co.in/v1/dropdown/user?designation=Trainee";
//		RestTemplate restTemplate = new RestTemplate();
//		String apiResponse = restTemplate.getForObject(apiUrl, String.class);
//		ObjectMapper mapper = new ObjectMapper();
//		JsonNode responseNode = mapper.readTree(apiResponse);
//		JsonNode dataArray = responseNode.get("data");
//
//		for (JsonNode node : dataArray) {
//			UserDto userDto = UserDto.builder()
//					.token(null)
//					._id(node.get("_id").asText())
//					.name(node.get("name").asText())
//					.empCode(node.get("employeeId").asText())
//					.teamId((node.get("teams").asText()))
//					.teamName((node.get("teamNames").asText()))
//					.build();
//			traineeIdAndUserMap.put(userDto.get_id(), userDto);
//		}
	}
	public static String searchUserById(String userId) {
		UserDto userDto = idUserMap.get(userId);
		if(userId == null || userId.isEmpty() || userDto == null) {
			return "User not found";
		}
		return userDto.getName();
	}
	public static String searchTeamById(String id) {
		return teamIdAndNameMap.getOrDefault(id, "User not found");
	}
	public static void main(String[] args) throws JsonProcessingException {
		findUsersAndMap();
		findTeamsAndMap();
		findTraineeAndMap();
		zoneCategoryMap.put(1, "1st Zone");
		zoneCategoryMap.put(2, "2nd Main Zone");
		zoneCategoryMap.put(3, "2nd Side Zone");
		zoneCategoryMap.put(4, "3rd Main Zone");
		zoneCategoryMap.put(5, "3rd Side Zone");
		zoneCategoryMap.put(6, "4th Zone");
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

	public static List<UserDto> getTraineeList() {
		return null;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
