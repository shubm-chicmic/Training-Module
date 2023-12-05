package com.chicmic.trainingModule.Filters;


import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Service.UserServiceImpl;
import com.chicmic.trainingModule.Util.JwtUtil;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtill;
    private final UserServiceImpl userService;
    @Value("${server.servlet.context-path}")
    private String homePage;
    private RedirectStrategy redirectStrategy=new DefaultRedirectStrategy();
    public  CustomAuthorizationFilter(UserServiceImpl userService,JwtUtil jwtUtill){
        this.userService=userService;
        this.jwtUtill=jwtUtill;
    }
    private static final String X= "You are not authorized to access this route !";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        log.info("visited url = " + servletPath);
        if (servletPath.contains("/v1/training")) {
            String AuthorizationHeader = request.getHeader("Authorization");
            String userMetadata = request.getHeader("userMetadata");
            // find data
            System.out.println("Authorization header = " + AuthorizationHeader);

//            Boolean isValidToken = validateToken(AuthorizationHeader);
            Boolean isValidToken = true;
            //if authorization header is invalid or null!!!
            if(!isValidToken){
                System.out.println("control inside@@@");
                Map<String,String> error=new HashMap<>();
                error.put("error","Please provide valid token");
//                token.put("refresh_token",refresh_token);
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(),error);
            }
            Collection<SimpleGrantedAuthority> authorities=new ArrayList<>();
//            authorities.add(new SimpleGrantedAuthority("ROLE_"+role.toUpperCase()));
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=
                    new UsernamePasswordAuthenticationToken(user.get_id(),null,authorities);
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    public static String getTokenFromCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    private void setPrevUrlCookie(HttpServletResponse response, String servletPath) {
        Cookie prevUrl = new Cookie("prevURL", servletPath);
        prevUrl.setHttpOnly(true);
        prevUrl.setMaxAge(60 * 10);
        response.addCookie(prevUrl);
    }

    private UserDetails getUserDetailsFromToken(String token) {
        String username = jwtUtill.getUsernameFromToken(token);
        return StringUtils.isEmpty(username) ? null : userService.loadUserByUsername(username);
    }

    private boolean validateToken(String token, UserDetails userDetails) {
        return jwtUtill.validateJwtToken(token, userDetails);
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        clearPrevUrlCookie(response);
    }

    private void clearPrevUrlCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("prevURL", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void handleForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        handleException(response, HttpStatus.FORBIDDEN, message);
    }


    private void handleException(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("text/html");
        response.getWriter().write("<html><body><h1>Error:</h1><p>"+message+"</p><br> <p> <a href='/api/v1/' >Login</a> here again</p></body></html>");
        response.getWriter().flush();
    }
}
