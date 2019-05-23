package com.psycorp.service.security.implementation;

import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {

//    private final CredentialsRepository credentialRepository;
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private TokenService tokenService;
//    private final PasswordEncoder passwordEncoder;

//    @Autowired
//    public AuthServiceImpl(
////            CredentialsRepository credentialRepository,
////                           PasswordEncoder passwordEncoder
//            TokenService tokenService) {
////        this.credentialRepository = credentialRepository;
////        this.passwordEncoder = passwordEncoder;
//        this.tokenService = tokenService;
//    }

    @Override
    public UserDetails getAuthPrincipal() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if(authentication.getPrincipal().equals("anonymousUser")) { // if user is not registered yet, then in Authentication principal="anonymousUser"
            return null;
        }
        return (UserDetails) authentication.getPrincipal();
//        UserDetails userDetails = userDetailsService.loadUserByUsername(principal);
//        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

//    @Override
//    public String generateAccessToken(UsernamePasswordDto usernamePassword) {
//
//        User user = userService.findUserByNameOrEmail(usernamePassword.getName());
//        CredentialsEntity credentialsEntity = credentialRepository.findByUserId(user.getId())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        validateUserPassword (usernamePassword.getPassword(), credentialsEntity.getPassword());
//        ObjectId userId = credentialsEntity.getUserId();
//
//       TokenEntity token =  tokenService.createUserToken(userId, TokenType.ACCESS_TOKEN);
//        return token.getToken();
//    }
//
//    @Override
//    public String generateAccessTokenForAnonim(User user) {
//       user = userService.find(user);
//       TokenEntity token =  tokenService.createUserToken(user.getId(), TokenType.ACCESS_TOKEN);
//       return token.getToken();
//    }

//    @Override
//    public User getUserByToken(String token){
//        return tokenService.getUserByToken(token);
//    }
//
//    @Override
//    public Boolean ifExistTokenByTypeAndToken(TokenType type, String token){
//        return tokenService.ifExistByTypeAndToken(type, token);
//    }

//    @Override
//    public String generateDeviceToken() {
//        return tokenService.createJwtToken();
//    }

//    private void validateUserPassword (String incomingPassword, String storedPassword) {
//        if(!passwordEncoder.matches(incomingPassword, storedPassword)) throw new BadCredentialsException("Bad Credentials");
//    }
}
