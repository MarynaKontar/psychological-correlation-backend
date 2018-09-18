package com.psycorp.service.security;


import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;

public interface TokenService {
    TokenEntity createUserToken(User userEntity, TokenType tokenType);
//    String createJwtToken();
}
