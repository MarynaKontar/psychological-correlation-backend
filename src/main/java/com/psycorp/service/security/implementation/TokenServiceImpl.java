package com.psycorp.service.security.implementation;

import com.mongodb.client.result.UpdateResult;
import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.service.UserService;
import com.psycorp.service.security.TokenService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

/**
 * Service implementation for TokenService.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Service
@PropertySource(value = {"classpath:common.properties"}, encoding = "utf-8")
public class TokenServiceImpl implements TokenService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private final TokenRepository tokenRepository;
    private final UserService userService;
    private final CredentialsRepository credentialRepository;
    private final MongoOperations mongoOperations;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository,
                            UserService userService,
                            CredentialsRepository credentialRepository,
                            MongoOperations mongoOperations,
                            PasswordEncoder passwordEncoder,
                            Environment env) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.credentialRepository = credentialRepository;
        this.mongoOperations = mongoOperations;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    /**
     * Generates access token if name(or email) and password is valid.
     * @param usernamePassword name(or email) and password of user.
     * @return access token value if name(or email) and password is valid.
     */
    @Override
    @Transactional
    public String generateAccessToken(UsernamePasswordDto usernamePassword) {
        LOGGER.trace("generateAccessToken({})", usernamePassword);
        User user = userService.findUserByNameOrEmail(usernamePassword.getName());
        CredentialsEntity credentialsEntity = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        validateUserPassword (usernamePassword.getPassword(), credentialsEntity.getPassword());
        ObjectId userId = credentialsEntity.getUserId();
        tokenRepository.removeAllByUserId(userId);
        TokenEntity token =  createUserToken(userId, TokenType.ACCESS_TOKEN);
        LOGGER.trace("generateAccessToken: token: {}", token.getToken());

        return token.getToken();
    }

    /**
     * Generates refresh token for user.
     * @param user must not be {@literal null}.
     * @return refresh token value.
     * @throws  BadRequestException if user is {@literal null} or not exist.
     */
    @Override
    public String generateRefreshToken(User user) {
        if(user == null) {throw new BadRequestException(env.getProperty("error.UserCan`tBeNull"));}
        user = userService.find(user);
        TokenEntity token =  createUserToken(user.getId(), TokenType.REFRESH_TOKEN);
        return token.getToken();
    }

    /**
     * Generates access token for anonim user (that doesn't have password).
     * @param user saved in database anonim user.
     * If user is registered (has password) token will be returned too.
     * @return access token value.
     */
    @Override
    public TokenEntity generateAccessTokenForAnonim(User user) {
//        user = userService.find(user);
        TokenEntity token =  createUserToken(user.getId(), TokenType.ACCESS_TOKEN);
        return token;
    }

    /**
     * Gets user by token.
     * @param token the token used to receive the user
     * @return {@link User}, if token and user exist.
     * @throws AuthorizationException if none found.
     */
    @Override
    public User getUserByToken(String token){
        // TODO решить, что делать, если токена нет или истек срок
        TokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthorizationException("", ErrorEnum.TOKEN_EXPIRED));
       return userService.findById(tokenEntity.getUserId());
    }

    /**
     * Changes {@link TokenType} from INVITE to ACCESS.
     * If the type of token is already ACCESS, then it does not change.
     * Token value isn't changed.
     * @param token token whose type needs to be changed.
     * @throws AuthorizationException if none {@link TokenEntity} find for given token.
     * @throws BadRequestException if doesn't exist {@link TokenEntity}
     * not for{@link TokenType} ACCESS_TOKEN not for INVITE_TOKEN for this token value.
     */
    @Override
    public void changeInviteTokenToAccess(String token) {
        token = token.substring(ACCESS_TOKEN_PREFIX.length() + 1); //delete "Bearer "
        TokenEntity tokenEntity = tokenRepository.findByToken(token).orElseThrow(() -> new AuthorizationException("", ErrorEnum.TOKEN_NOT_FOUND));
        if (tokenEntity.getType().equals(TokenType.ACCESS_TOKEN)) { return; }
        if (tokenEntity.getType().equals(TokenType.INVITE_TOKEN)) {
            // UPDATE TOKENTYPE and EXPIRATIONDATE
            Update update = new Update().set("type", TokenType.ACCESS_TOKEN)
                    .set("expirationDate", getTokenExpirationDate(TokenType.ACCESS_TOKEN));
            Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(tokenEntity.getId()));
            UpdateResult updateResult = mongoOperations.updateFirst(query, update, TokenEntity.class);
        } else throw new BadRequestException("tokenEntity with INVITE_TOKEN type isn't exist for token: " + tokenEntity.getToken());
    }

    /**
     * Retrieves {@link TokenEntity} by {@link User} id.
     * @param userId must not be {@literal null}.
     * @return {@link TokenEntity} if exist or throws {@link BadRequestException}0 if not.
     * @throws BadRequestException if token entity isn't exist for userId.
     */
    @Override
    public TokenEntity findByUserId(ObjectId userId) {
        return tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.ThereIsn'tToken") + "for userId: " + userId));
    }

    /**
     * Retrieves {@link TokenEntity} by {@link User} id and {@link TokenType}.
     * @param userId user id.
     * @param tokenType type of token.
     * @return {@link TokenEntity} if exist or {@literal null} if not.
     */
    @Override
    public TokenEntity findByUserIdAndTokenType(ObjectId userId, TokenType tokenType) {
        return tokenRepository.findByUserIdAndType(userId, tokenType)
                .orElse(null);
    }

    /**
     * Gets token (or creates it, if not exist) for {@link User} with userId.
     * @param token token of the registered user or null if token doesn't exist.
     * @param userId user id for whom token is getting or creating; must not be {@literal null}.
     * @return token value if userId and token is valid.
     * @throws BadRequestException if tokenType or userId is {@literal null} or there isn't user for userId.
     */
    @Override
    @Transactional
    public String getTokenForRegisteredUser(String token, ObjectId userId) {
        //user is registered before pass test and doesn't have token yet
        if (token == null) {
            return ACCESS_TOKEN_PREFIX + " " +
                    createUserToken(userId, TokenType.ACCESS_TOKEN).getToken();
            //TODO проверять по токену или userId?
        } else { //user has token (passed test(access token) or took invite from friend (invite token) but didn't pass test yet or tested on the same computer)
            token = token.substring(ACCESS_TOKEN_PREFIX.length() + 1); //delete "Bearer "
            TokenEntity accessTokenEntity = findByUserIdAndTokenType(userId, TokenType.ACCESS_TOKEN);
            TokenEntity inviteTokenEntity = findByUserIdAndTokenType(userId, TokenType.INVITE_TOKEN);
            if ( accessTokenEntity == null && inviteTokenEntity == null) { //token had expired (token came from frontend, but it doesn't in db(expired and deleted))
                return ACCESS_TOKEN_PREFIX + " " +
                    createUserToken(userId, TokenType.ACCESS_TOKEN).getToken();
            } else if (accessTokenEntity != null && accessTokenEntity.getToken().equals(token)) {// user passed test -> has access token and this token equals to token (that came from frontend)
                return ACCESS_TOKEN_PREFIX + " " + accessTokenEntity.getToken();
            } else if (inviteTokenEntity != null && inviteTokenEntity.getToken().equals(token)) {// user has only invite token (took it from friend and didn't pass test yet)
                changeInviteTokenToAccess(ACCESS_TOKEN_PREFIX + " " + inviteTokenEntity.getToken());
                return ACCESS_TOKEN_PREFIX + " " + inviteTokenEntity.getToken();
            } else {
                throw new BadRequestException("Token isn't valid: " + token);
            }
        }
    }

    /**
     * Saves n tokens with {@link TokenType} INVITE for principal user and creates list of these tokens values.
     * If n > 5 than n = 5.
     * @param n number of token's values to create; must not be {@literal null}
     * @return list of tokens values with size of n.
     * @throws BadRequestException if n is {@literal null} or less than 1.
     */
    @Override
    @Transactional
    public List<String> generateInviteTokenList(Integer n){ //n tokens
        if (n == null || n < 1) { throw new BadRequestException("Number of generated invite tokens must be 1 and more. n = " + n); }
        if(n > 5) {n = 5;}
        User principal = userService.getPrincipalUser();
        List<ObjectId> usersForMatchingId = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            usersForMatchingId.add(userService.createAnonimUser().getId()); // creates anonim user
        }

        List<String> tokens= new ArrayList<>(n);
        usersForMatchingId.forEach(userId -> {
            tokens.add(createUserToken(userId, TokenType.INVITE_TOKEN).getToken());// CREATE INVITE TOKENS FOR ALL USERSFORMATCHING
            mongoOperations.updateFirst(  // UPDATE: add principalId to usersForMatchingId for all n users (add them principalId)
                    Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userId)),
                    new Update().push("usersForMatchingId").each(Collections.singletonList(principal.getId())),
                    User.class);
        });

        // UPDATE USERSFORMATCHING for principal user
        Update updateUser = new Update().push("usersForMatchingId").atPosition(Update.Position.LAST).each(usersForMatchingId);
        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(principal.getId()));
        mongoOperations.updateFirst(query, updateUser, User.class);

        return tokens;
    }

    /**
     * Creates and saves {@link TokenEntity} with {@link TokenType} tokenType for {@link User} with userId.
     * @param userId id of user for whom {@link TokenEntity} is created, must not be {@literal null}.
     * @param tokenType type of token, must not be {@literal null}.
     * @return the saved {@link TokenEntity} will never be {@literal null}.
     * @throws BadRequestException if tokenType or userId is {@literal null} or there isn't user for userId.
     */
    private TokenEntity createUserToken(ObjectId userId, TokenType tokenType) {
        if (userId == null || tokenType == null) {
            throw new BadRequestException("User id or token type cann't be null: userId: " + userId + ", tokenType: " + tokenType);
        }
        if(!userService.checkIfExistById(userId)) {
            throw new BadRequestException(env.getProperty("error.noUserFound") + "for userId: " + userId);
        }

        String token = UUID.randomUUID().toString();
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setUserId(userId);
        tokenEntity.setType(tokenType);
        tokenEntity.setExpirationDate(getTokenExpirationDate(tokenType));
        tokenRepository.save(tokenEntity);

        return tokenEntity;
    }

    /**
     * Gets expiration date for all {@link TokenType}.
     * @param tokenType token type.
     * @return {@link LocalDateTime} expiration date of tiken with {@link TokenType} tokenType.
     */
    private LocalDateTime getTokenExpirationDate(TokenType tokenType) {
        Integer days;
        switch (tokenType) {
            case INVITE_TOKEN: {
                days = Integer.valueOf(env.getProperty("invite.token.expiration.date"));
                break;
            }
            case ACCESS_TOKEN: {
                days = Integer.valueOf(env.getProperty("access.token.expiration.date"));
                break;
            }
            case REFRESH_TOKEN: {
                days = Integer.valueOf(env.getProperty("refresh.token.expiration.date"));
                break;
            }
            default: {
                days = 0;
            }
        }
        return LocalDateTime.now().plusDays(days);
    }

    /**
     * Verifies  the encoded password obtained from storage matches the submitted raw
     * password after it too is encoded. The stored password itself is never decoded.
     * @param rawPassword the raw password to encode and match.
     * @param storedPassword the encoded password from storage to compare with.
     * @throws BadCredentialsException if passwords don't match.
     */
    private void validateUserPassword (String rawPassword, String storedPassword) {
        if(!passwordEncoder.matches(rawPassword, storedPassword)) throw new BadCredentialsException("Bad Credentials");
    }
}
