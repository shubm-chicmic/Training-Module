package com.chicmic.trainingModule.trainingModule.Service;

import com.chicmic.trainingModule.trainingModule.Filters.CustomAuthenticationFilter;
import com.chicmic.trainingModule.trainingModule.Filters.CustomAuthorizationFilter;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.chicmic.trainingModule.trainingModule.Util.JwtUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserDetailsService {
    private final JwtUtil jwtUtil;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final String email=username;
//        final Collection<Authority> authorities=new ArrayList<>();
//        if(usersRepo.findByEmail(email)==null ) {
//            throw new UsernameNotFoundException("User with this email is not registered as SuperUser ");
//        }
//        else{
//            Users users= usersRepo.findByEmail(email);
//            users.getUsersProfileList().forEach(usersProfile -> authorities.add(new Authority(UserType.valueOf(usersProfile.getUserType()).toString())));
//            return new User(email,users.getPassword(),authorities);
//            }
        return null;
    }

//    public Users save(UserDto user){
//        return usersRepo.save(user);
//    }
//    public Users getUserByEmail(String username) {
//        return usersRepo.findByEmail(username);
//    }
//    public Users getUserById(Long id) {
//        return usersRepo.findById(id).orElse(null);
//    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies=request.getCookies();
        if(cookies!=null){
            for(Cookie cookie:cookies){
                if("Authorization".equals(cookie.getName())){
                    cookie.setValue(null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);break;
                }
            }
        }
    }
    public String getTokenByLink(String link) {
        char ch='a';
        int i=0;
        while(ch!='/'){
            ch=link.charAt(i++);
        }
        return link.substring(i);
    }


    public void updateToken(HttpServletRequest request,HttpServletResponse response, String email) {
        String token= CustomAuthorizationFilter.getTokenFromCookies(request.getCookies());
        String updatedToken= jwtUtil.updateJwtToken(token,email);
        CustomAuthenticationFilter.setAuthorizationCookie(response,updatedToken);
    }

//    @Transactional
//    public void createUser(){
//        Users users=new Users();
//        users.setId(999l);
//        users.setDateJoined(LocalDateTime.now().minusMonths(10));
//        users.setIsStaff(false);
//        users.setEmail("superadminUday@yopmail.com");
//        users.setPassword(passwordEncoder().encode("10032002"));
//        users.setUsername("udxsh");
//        users.setFirstName("Uday");
//        users.setLastName("Sharma");
//        users.setIsActive(true);
//        users.setLastLogin(LocalDateTime.now());
//        users.setIsSuperuser(false);
//        usersRepo.save(users);
//
//        UsersProfile usersProfile=new UsersProfile();
//        usersProfile.setCreatedAt(LocalDateTime.now().minusMonths(10));
//        usersProfile.setUsers(users);
//        usersProfile.setUpdatedAt(LocalDateTime.now());
//        usersProfile.setId(999l);
//        usersProfile.setUuid(UUID.randomUUID().toString().substring(0,6));
//        usersProfile.setYearOfJoining(2023);
//        usersProfile.setUserType(1);
//        usersProfile.setVerified(true);
//        usersProfile.setGender("M");
//        usersProfile.setDeviceType(1);
//        usersProfileRepo.save(usersProfile);
//    }

}
