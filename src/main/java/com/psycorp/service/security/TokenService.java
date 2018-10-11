package com.psycorp.service.security;


import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;

import java.util.List;

public interface TokenService {
    TokenEntity createUserToken(User userEntity, TokenType tokenType);

    List<String> generateTokenList(Integer n);

    User getUserByToken(String token);
//    String createJwtToken();
}
