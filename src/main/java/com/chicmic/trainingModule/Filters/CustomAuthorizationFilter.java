package com.chicmic.trainingModule.Filters;


import com.chicmic.trainingModule.Config.Security.CustomPermissionEvaluator;
import com.chicmic.trainingModule.Service.UserServiceImpl;
import com.chicmic.trainingModule.Util.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.sortBySpecificity;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final UserServiceImpl userService;
    @Value("${server.servlet.context-path}")
    private String homePage;
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private static final String X= "You are not authorized to access this route !";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        log.info("visited url = " + servletPath);

        if (servletPath.contains("/v1/training")||servletPath.contains("/v2/training")) {
            String authorizationHeader = request.getHeader("Authorization");
            String userMetadataHeader = request.getHeader("userMeta");

            if (authorizationHeader != null && userMetadataHeader != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> userMetadataMap = objectMapper.readValue(userMetadataHeader, new TypeReference<Map<String, Object>>() {});
                Map<String, Object> userMetaData = (Map<String, Object>) userMetadataMap.get("user");
                Map<String, Object> userData = (Map<String, Object>) userMetaData.get("data");
                String userId = (String) userData.get("_id");
                System.out.println("\u001B[33m userId = " + userId+ "\u001B[0m");
                String userRole = null;
                List<Map<String, Object>> userRoleDataList = (List<Map<String, Object>>) userData.get("roleData");
                if (userRoleDataList != null && !userRoleDataList.isEmpty()) {
                    for (Map<String, Object> userRoleData : userRoleDataList) {
                        userRole = (String) userRoleData.get("role");
                        System.out.println("\u001B[33m User Role = " + userRole + "\u001B[0m");
                    }
                }
                Map<String, Boolean> permissions = (Map<String, Boolean>) userData.get("permissions");
                CustomPermissionEvaluator.permissions = permissions;
                Boolean isValidToken = true;//validateToken(authorizationHeader);

                if (isValidToken) {
                    // Authentication is successful, proceed to set up authentication
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    // Add user roles or authorities if available
                    authorities.add(new SimpleGrantedAuthority( userRole.toUpperCase()));
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userId, authorizationHeader, authorities);

                    // Set authentication details
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    // Continue filter chain
                    System.out.println("control reaches here");
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            // If token is invalid or headers are missing, return error response
            Map<String, String> error = new HashMap<>();
            error.put("error", "Please provide valid token and user metadata");
            response.setContentType(APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), error);
        } else {
            System.out.println("Control reaches here!!---");
            filterChain.doFilter(request, response);
        }
    }




    private void handleForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        handleException(response, HttpStatus.FORBIDDEN, message);
    }


    private void handleException(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");

        // Create the error response structure
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        // Write the error response to the servlet response output stream
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
