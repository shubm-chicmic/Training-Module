package com.chicmic.trainingModule.Config.Security;


import com.chicmic.trainingModule.Filters.CustomAuthenticationFilter;
import com.chicmic.trainingModule.Filters.CustomAuthorizationFilter;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.JwtUtil;
import com.chicmic.trainingModule.Service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableTransactionManagement
public class SecurityConfig {

    private final JwtUtil jwtProvider;
    private final UserServiceImpl userService;
//    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(TrainingModuleApplication.passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter filter=new CustomAuthenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)),jwtProvider,userService);

        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(requests->requests.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll());
        http.authorizeHttpRequests(requests->requests.requestMatchers(HttpMethod.POST,"/v1/training/userProgress").permitAll());
        http.authorizeHttpRequests(requests->requests.requestMatchers("/v1/training/**").authenticated());
        //        http.authorizeHttpRequests(requests->requests.requestMatchers("/v2/training","/v2/training/**").authenticated());
//        http.authorizeHttpRequests(requests->requests
//                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
//                        .antMatchers("/v1/training/feedback").hasAnyAuthority("TL", "PA", "PM", "IND")
//                        .antMatchers("/v1/training/**").hasAnyAuthority("TL", "PA", "PM")
////                .requestMatchers(HttpMethod.POST,"/v1/training/**").hasAnyAuthority("TL", "PA", "PM")
////                .requestMatchers(HttpMethod.DELETE,"/v1/training/**").hasAnyAuthority("TL", "PA", "PM")
//        );
//        http.authorizeHttpRequests(requests->requests.requestMatchers(HttpMethod.POST,"/v1/training/**").hasAnyAuthority("TL", "PA", "PM"));
//        http.authorizeHttpRequests(requests->requests.requestMatchers(HttpMethod.DELETE,"/v1/training/**").hasAnyAuthority("TL", "PA", "PM"));

        http.authorizeHttpRequests(requests->requests.requestMatchers("/addCourseWithScript").permitAll());
//        http.authorizeHttpRequests(requests->requests.requestMatchers("/v1/training/course","/favicon.ico","/api/health-check").permitAll());
        http.authorizeHttpRequests(requests->requests.anyRequest().permitAll());

        //adding filters
        http.addFilterBefore(new CustomAuthorizationFilter(userService), UsernamePasswordAuthenticationFilter.class);
        http.addFilter(filter);

        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public PermissionEvaluator permissionEvaluator() {
        return new CustomPermissionEvaluator();
    }
}
