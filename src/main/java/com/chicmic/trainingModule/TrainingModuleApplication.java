package com.chicmic.trainingModule;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.DbVersion;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.DatabaseVersionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.CommandLineRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootApplication
@EnableScheduling
@Log4j2
public class TrainingModuleApplication implements CommandLineRunner {
	private static String apiGateWayUrl;
	@Autowired
	private DatabaseVersionService databaseVersionService;
	@Autowired
	private CourseService courseService;

	@Value("${apiGatewayUrl}")
	private void setApiGateWayUrl(String apiGatewayUrl) {
		TrainingModuleApplication.apiGateWayUrl = apiGatewayUrl;
	}
	public static final Map<String, UserDto> idUserMap = new HashMap<>();
	public static final Map<String, String> teamIdAndNameMap = new HashMap<>();
	public static HashMap<Integer, String> zoneCategoryMap = new HashMap<>();
	private static void findUsersAndMap() throws JsonProcessingException {
		String apiUrl = apiGateWayUrl + "/v1/dropdown/user";
		System.out.println("api url " + apiUrl);
		RestTemplate restTemplate = new RestTemplate();
		String apiResponse = restTemplate.getForObject(apiUrl, String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseNode = mapper.readTree(apiResponse);
		JsonNode dataArray = responseNode.get("data");

		for (JsonNode node : dataArray) {
			List<String> teams = new ArrayList<>();
			JsonNode teamsNode = node.get("teams");
			if (teamsNode.isArray()) {
				for (JsonNode team : teamsNode) {
					teams.add(team.asText());
				}
			}
			UserDto userDto = UserDto.builder()
					.token(null)
					._id(node.get("_id").asText())
					.name(node.get("name").asText())
					.empCode(node.get("employeeId").asText())
					.teams(teams)
					.teamName((node.get("teamNames").asText()))
					.build();
			idUserMap.put(userDto.get_id(), userDto);
		}
	}
	private static void findTeamsAndMap() throws JsonProcessingException {
		String apiUrl = apiGateWayUrl + "/v1/dropdown/team";
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
		System.out.println("\u001B[31m fetching trainee list");
		String apiUrl = apiGateWayUrl + "/v1/dropdown/user?designation=Trainee";
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
			List<String> teams = new ArrayList<>();
			JsonNode teamsNode = node.get("teams");
			if (teamsNode.isArray()) {
				for (JsonNode team : teamsNode) {
					teams.add(team.asText());
				}
			}
			UserDto userDto = UserDto.builder()
					.token(null)
					._id(node.get("_id").asText())
					.name(node.get("name").asText())
					.empCode(node.get("employeeId").asText())
					.teams(teams)
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
		SpringApplication.run(TrainingModuleApplication.class, args);
	}
	@Override
	public void run(String... args) throws Exception {
		runMigrations();
		runScheduledTasks();
	}
	private void runMigrations() {
		// Use the databaseVersionService to save or update the database version
		DbVersion dbVersion = databaseVersionService.getDatabaseVersion();
		if(dbVersion != null){
			if(dbVersion.getVersion() < 1) {
				try {
					String message = addCourseFromExcelToDatabase();
					log.info(message);
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					databaseVersionService.updateDatabaseVersion();
				}
			}
			return;
		}
		dbVersion = databaseVersionService.saveDbVersion();
		String message = addCourseFromExcelToDatabase();
		log.info(message);

	}
	public String addCourseFromExcelToDatabase(){
		try {
			Path excelSearchPath = Paths.get("TrainingModuleExcelSearch");

			final Integer[] count = {0};
			Files.walk(excelSearchPath)
					.filter(Files::isRegularFile)
					.filter(path -> path.toString().toLowerCase().endsWith(".xlsx"))
					.forEach(path -> {
						System.out.println("Processing file: " + path);
						Course course = ExcelPerformOperations.excelPerformOperations(path.toString());
						System.out.println(course.getName());
						courseService.createCourse(course);
						count[0]++;
					});
			return count[0] +" courses created successfully";
		} catch (IOException e) {
			e.printStackTrace();
			return "failed because of " + e.getMessage();
		}
	}
	@Scheduled(fixedDelay = 4 * 60 * 60 * 1000)
	private void runScheduledTasks() throws Exception {
		idUserMap.clear();
		teamIdAndNameMap.clear();
		zoneCategoryMap.clear();
		findUsersAndMap();
		findTeamsAndMap();
		zoneCategoryMap.put(1, "1st Zone");
		zoneCategoryMap.put(2, "2nd Main Zone");
		zoneCategoryMap.put(3, "2nd Side Zone");
		zoneCategoryMap.put(4, "3rd Main Zone");
		zoneCategoryMap.put(5, "3rd Side Zone");
		zoneCategoryMap.put(6, "4th Zone");
		System.out.println("\u001B[31m VAlue =================== " + zoneCategoryMap.get(4) + "\u001B[0m");

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
