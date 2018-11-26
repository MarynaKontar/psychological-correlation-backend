package com.psycorp.util;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.security.AuthService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

//    private final AuthService authService;
//
//    @Autowired
//    public AuthUtil(AuthService authService) {
//        this.authService = authService;
//    }

//    public void userAuthorization(ObjectId userId) {
//        TokenPrincipal tokenPrincipal = (TokenPrincipal)(authService.getAuthPrincipal());
//        if( !tokenPrincipal.getId().equals(userId) ) {
//            //TODO какой exception кидать?
//            throw new AuthorizationException("Access Denied", ErrorEnum.NOT_ENOUGH_PERMISSIONS);
//        }
//    }

//    public TokenPrincipal getTokenPrincipal(){
//        UserDetails userDetails = authService.getAuthPrincipal();
//        TokenPrincipal tokenPrincipal = (TokenPrincipal) userDetails;
//       return  tokenPrincipal;
////       return (TokenPrincipal) authService.getAuthPrincipal();
//
//    }
}
