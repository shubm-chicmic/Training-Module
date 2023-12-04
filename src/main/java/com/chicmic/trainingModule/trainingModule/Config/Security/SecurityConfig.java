package com.chicmic.trainingModule.trainingModule.Config.Security;


import com.chicmic.trainingModule.trainingModule.Filters.CustomAuthenticationFilter;
import com.chicmic.trainingModule.trainingModule.Filters.CustomAuthorizationFilter;
import com.chicmic.trainingModule.trainingModule.Service.UserServiceImpl;
import com.chicmic.trainingModule.trainingModule.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.chicmic.trainingModule.trainingModule.TrainingModuleApplication.passwordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtProvider;
    private final UserServiceImpl userService;
//    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter filter=new CustomAuthenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)),jwtProvider,userService);
        filter.setFilterProcessesUrl("/login");
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(requests->requests.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll());
        http.authorizeHttpRequests(authorizeHttpRequests->authorizeHttpRequests.anyRequest().permitAll());

        //adding filters
        http.addFilterBefore(new CustomAuthorizationFilter(jwtProvider,userService), UsernamePasswordAuthenticationFilter.class);
        http.addFilter(filter);

        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
