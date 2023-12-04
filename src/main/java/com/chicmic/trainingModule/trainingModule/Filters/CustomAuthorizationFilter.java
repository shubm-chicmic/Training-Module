package com.chicmic.trainingModule.trainingModule.Filters;


import com.chicmic.trainingModule.trainingModule.Service.UserServiceImpl;
import com.chicmic.trainingModule.trainingModule.Util.JwtUtil;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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
//        log.info("visited url = " + servletPath);

        if (servletPath.contains("/admin/")) {
            String token = getTokenFromCookies(request.getCookies());

            if (token.isEmpty()) {
                setPrevUrlCookie(response, request.getServletPath());
                response.setStatus(401);
                redirectStrategy.sendRedirect(request, response, "/");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = getUserDetailsFromToken(token);
                    if (userDetails != null) {
                        if (validateToken(token, userDetails)) {
                            setAuthentication(userDetails, request, response);
                        } else {
                            response.setStatus(401);
                            handleForbiddenResponse(response, "Invalid token");
                            return;
                        }
                    } else {
                        response.setStatus(401);
                        handleForbiddenResponse(response, "Invalid token");
                        return;
                    }
                } catch (ExpiredJwtException e) {
                    response.setStatus(401);
                    handleForbiddenResponse(response, "Your token has expired, please log in again");
                    return;
                } catch (SignatureException e) {
                    response.setStatus(401);
                    handleForbiddenResponse(response, "Invalid token signature");
                    return;
                }
            }
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
