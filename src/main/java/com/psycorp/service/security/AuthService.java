package com.psycorp.service.security;

import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {

//    default UserDetails getAuthPrincipal() {
//        SecurityContext securityContext = SecurityContextHolder.getContext();
//        Authentication authentication = securityContext.getAuthentication();
//        String principal = (String) authentication.getPrincipal();
//
//
//
//        return userDetails;
////        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }

    UserDetails getAuthPrincipal();

    String generateAccessToken(UsernamePasswordDto usernamePassword);

    String generateAccessTokenForAnonim(User user);

    User getUserByToken(String token);

    Boolean ifExistTokenByTypeAndToken(TokenType type, String token);
//    String generateDeviceToken();
}
