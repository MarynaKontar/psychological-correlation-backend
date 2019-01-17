package com.psycorp.service.security;


import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenService {
    TokenEntity createUserToken(User userEntity, TokenType tokenType);

    LocalDateTime getTokenExpirationDate(TokenType tokenType);

    List<String> generateInviteTokenList(Integer n);

    User getUserByToken(String token);

    Boolean ifExistByTypeAndToken(TokenType type, String token);

    TokenEntity getByTypeAndToken(TokenType type, String token);
//    String createJwtToken();
}
