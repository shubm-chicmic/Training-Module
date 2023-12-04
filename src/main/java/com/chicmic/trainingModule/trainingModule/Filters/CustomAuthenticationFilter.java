package com.chicmic.trainingModule.trainingModule.Filters;

import com.chicmic.trainingModule.trainingModule.Service.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.chicmic.trainingModule.trainingModule.Util.JwtUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserServiceImpl userService;
    private RedirectStrategy redirectStrategy=new DefaultRedirectStrategy();

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtProvider, UserServiceImpl userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtProvider;
        this.userService = userService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        final String email = request.getParameter("username");
        final String password = request.getParameter("password");

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        return authenticationManager.authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String email = authResult.getName();
        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("roles", roles);

        String jwtToken = jwtUtil.createJwtToken(payload, email);

        // Determine the redirect URL
        String redirectUrl = determineRedirectUrl(request, "https");

        // Clearing JSESSIONID cookie
        clearSessionCookie(request, response);

        // Setting the Authorization cookie
        setAuthorizationCookie(response, jwtToken);

        // Setting authenticated user in SecurityContextHolder
        setAuthenticatedUserInSecurityContext(authResult);

        // Redirecting user
        redirectStrategy.sendRedirect(request, response, redirectUrl);
    }

    private String determineRedirectUrl(HttpServletRequest request, String protocol) {
        System.out.println("\u001B[31m REdirect url = " + protocol);
        String defaultRedirectPath = "/admin/dashboard";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("prevURL".equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                    // Construct the redirect URL by preserving the context path and port
                    String contextPath = request.getContextPath();
                    String previousPath = cookie.getValue();
                    if (previousPath.startsWith("/")) {
                        previousPath = previousPath.substring(1);
                    }
                    return protocol + "://" + request.getServerName() + ":" + request.getServerPort() + contextPath + "/" + previousPath;
                }
            }
        }
        // Construct the defaultRedirectUrl by preserving the context path and port
        String contextPath = request.getContextPath();
        System.out.println("context path " + contextPath);

        if (defaultRedirectPath.startsWith("/")) {
            defaultRedirectPath = defaultRedirectPath.substring(1);
        }
        System.out.println("Port = " + request.getServerPort());
        String redirectUrl = "";
        if(request.getServerName().equals("localhost")){
            redirectUrl = "http://" + request.getServerName() + ":" +  request.getServerPort() + contextPath + "/" + defaultRedirectPath;
        }else {
            redirectUrl = protocol + "://" + request.getServerName() + contextPath + "/" + defaultRedirectPath;
        }
        System.out.println("Redirecting to: " + redirectUrl + "\u100B[0m");
        return redirectUrl;
    }



    private void clearSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    public static void setAuthorizationCookie(HttpServletResponse response, String jwtToken) {
        Cookie cookie = new Cookie("Authorization", jwtToken);
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void setAuthenticatedUserInSecurityContext(Authentication authResult) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authResult, null, authResult.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401);
    }


}
