package com.psycorp.service.security.implementation;

import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {

    private final CredentialsRepository credentialRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(CredentialsRepository credentialRepository,
                           PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails getAuthPrincipal() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if(authentication.getPrincipal().equals("anonymousUser")) { // if user is not registered yet
            return null;
        }
        return (UserDetails) authentication.getPrincipal();
//        UserDetails userDetails = userDetailsService.loadUserByUsername(principal);
//        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public String generateAccessToken(UsernamePasswordDto usernamePassword) {

//        User user = userRepository.findFirstByName(usernamePassword.getName())
        //TODO findUserByNameOrEmail не работает, если не писать два раза (и для name и для email)
        User user = userService.findUserByNameOrEmail(usernamePassword.getName());
        CredentialsEntity credentialsEntity = credentialRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        validateUserPassword (usernamePassword.getPassword(), credentialsEntity.getPassword());
        user = credentialsEntity.getUser();

       TokenEntity token =  tokenService.createUserToken(user, TokenType.ACCESS_TOKEN);
        return token.getToken();
    }

    @Override
    public String generateAccessTokenForAnonim(User user) {
       user = userService.find(user);
       TokenEntity token =  tokenService.createUserToken(user, TokenType.ACCESS_TOKEN);
       return token.getToken();
    }

    @Override
    public User getUserByToken(String token){
        return tokenService.getUserByToken(token);
    }

//    @Override
//    public String generateDeviceToken() {
//        return tokenService.createJwtToken();
//    }

    private void validateUserPassword (String incomingPassword, String storedPassword) {
        if(!passwordEncoder.matches(incomingPassword, storedPassword)) throw new BadCredentialsException("Bad Credentials");
    }
}
