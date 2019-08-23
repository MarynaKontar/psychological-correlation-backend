package com.psycorp.service.security;


import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Service interface for {@link TokenEntity}.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
public interface TokenService {

    /**
     * Generates access token if name(or email) and password is valid.
     * @param usernamePassword name(or email) and password of user.
     * @return access token value if name(or email) and password is valid.
     */
    String generateAccessToken(UsernamePasswordDto usernamePassword);

    /**
     * Generates refresh token for user.
     * @param user must not be {@literal null}.
     * @return refresh token value.
     * @throws BadRequestException if user is {@literal null} or not exist.
     */
    String generateRefreshToken(User user);

    /**
     * Generates access token for anonim user (that doesn't have password).
     * @param user saved in database anonim user.
     * If user is registered (has password) token will be returned too.
     * @return access token value.
     */
    TokenEntity generateAccessTokenForAnonim(User user);

    /**
     * Gets user by token.
     * @param token the token used to receive the user
     * @return {@link User}, if token and user exist.
     * @throws AuthorizationException if none found.
     */
    User getUserByToken(String token);

    /**
     * Changes {@link TokenType} from INVITE to ACCESS.
     * If the type of token is already ACCESS, then it does not change.
     * Token value isn't changed.
     * @param token token whose type needs to be changed.
     * @throws AuthorizationException if none {@link TokenEntity} find for given token.
     * @throws BadRequestException if doesn't exist {@link TokenEntity}
     * not for{@link TokenType} ACCESS_TOKEN not for INVITE_TOKEN for this token value.
     */
    void changeInviteTokenToAccess(String token);

    /**
     * Retrieves {@link TokenEntity} by {@link User} id.
     * @param userId must not be {@literal null}.
     * @return {@link TokenEntity} if exist or throws {@link BadRequestException}0 if not.
     * @throws BadRequestException if token entity isn't exist for userId.
     */
    TokenEntity findByUserId(ObjectId userId);

    /**
     * Retrieves {@link TokenEntity} by {@link User} id and {@link TokenType}.
     * @param userId user id.
     * @param tokenType type of token.
     * @return {@link TokenEntity} if exist or {@literal null} if not.
     */
    TokenEntity findByUserIdAndTokenType(ObjectId userId, TokenType tokenType);

    /**
     * Gets token (or creates it, if not exist) for {@link User} with userId.
     * @param token token of the registered user or null if token doesn't exist.
     * @param userId user id for whom token is getting or creating; must not be {@literal null}.
     * @return token value if userId and token is valid.
     * @throws BadRequestException if tokenType or userId is {@literal null} or there isn't user for userId.
     */
    String getTokenForRegisteredUser(String token, ObjectId userId);

    /**
     * Saves n tokens with {@link TokenType} INVITE for principal user and creates list of these tokens values.
     * If n > 5 than n = 5.
     * @param n number of token's values to create; must not be {@literal null}
     * @return list of tokens values with size of n.
     * @throws BadRequestException if n is {@literal null} or less than 1.
     */
    List<String> generateInviteTokenList(Integer n);

}
