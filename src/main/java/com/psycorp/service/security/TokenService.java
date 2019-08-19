package com.psycorp.service.security;


import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Service interface for {@link TokenEntity}.
 * @author Vitaliy Proskura
 * @author  Maryna Kontar
 */
public interface TokenService {

    String generateAccessToken(UsernamePasswordDto usernamePassword);

    TokenEntity generateAccessTokenForAnonim(User user);

    User getUserByToken(String token);

    void changeInviteTokenToAccess(String token);

    TokenEntity findByUserId(ObjectId userId);

    TokenEntity findByUserIdAndTokenType(ObjectId userId, TokenType tokenType);

    String getTokenForRegisteredUser(String token, ObjectId userId);
//    String createJwtToken();

    List<String> generateInviteTokenList(Integer n);

    String generateRefreshToken(User user);
}
