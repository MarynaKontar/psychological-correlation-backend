package com.psycorp.service.security;


import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenService {
    TokenEntity createUserToken(User userEntity, TokenType tokenType);

    LocalDateTime getTokenExpirationDate(TokenType tokenType);

    List<String> generateInviteTokenList(Integer n);

    User getUserByToken(String token);

    void changeInviteTokenToAccess(String token);

    Boolean ifExistByTypeAndToken(TokenType type, String token);

    TokenEntity findByUserId(ObjectId userId);

    TokenEntity findByUserIdAndTokenType(ObjectId userId, TokenType tokenType);

//    String createJwtToken();
}
