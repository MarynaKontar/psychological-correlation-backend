package com.psycorp.service.security.implementation;

import com.mongodb.client.result.UpdateResult;
import com.psycorp.exception.AuthorizationException;
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

@Service
@PropertySource(value = {"classpath:common.properties"}, encoding = "utf-8")
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
//    @Autowired
    private final UserService userService;
    private final CredentialsRepository credentialRepository;
    private final MongoOperations mongoOperations;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository, UserService userService, CredentialsRepository credentialRepository, MongoOperations mongoOperations, PasswordEncoder passwordEncoder, Environment env) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.credentialRepository = credentialRepository;
        this.mongoOperations = mongoOperations;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @Override
    @Transactional
    public TokenEntity createUserToken(ObjectId userId, TokenType tokenType) {
        String token = UUID.randomUUID().toString();

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setUserId(userId);
        tokenEntity.setType(tokenType);
        tokenEntity.setExpirationDate(getTokenExpirationDate(tokenType));

        tokenRepository.save(tokenEntity);

        return tokenEntity;
    }

    @Override
    public LocalDateTime getTokenExpirationDate(TokenType tokenType) {
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

    @Override
    @Transactional
    public List<String> generateInviteTokenList(Integer n){ //n tokens
        User principal = userService.getPrincipalUser();
        List<ObjectId> usersForMatchingId = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            usersForMatchingId.add(userService.createAnonimUser().getId());
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

    @Override
    public User getUserByToken(String token){
        // TODO решить, что делать, если токена нет или истек срок
        TokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthorizationException("", ErrorEnum.TOKEN_EXPIRED));
       return userService.findById(tokenEntity.getUserId());
    }

    @Override
    public void changeInviteTokenToAccess(String token) {
        token = token.substring(ACCESS_TOKEN_PREFIX.length() + 1); //delete "Bearer "
        TokenEntity tokenEntity = tokenRepository.findByToken(token).orElseThrow(() -> new AuthorizationException("", ErrorEnum.TOKEN_NOT_FOUND));
        if (tokenEntity.getType().equals(TokenType.ACCESS_TOKEN)) { return; }
        if (ifExistByTypeAndToken(TokenType.INVITE_TOKEN, token)) {
            tokenEntity = getByTypeAndToken(TokenType.INVITE_TOKEN, token);

            // UPDATE TOKENTYPE and EXPIRATIONDATE
            Update update = new Update().set("type", TokenType.ACCESS_TOKEN)
                    .set("expirationDate", getTokenExpirationDate(TokenType.ACCESS_TOKEN));
            Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(tokenEntity.getId()));
            UpdateResult updateResult = mongoOperations.updateFirst(query, update, TokenEntity.class);
        }
    }

    @Override
    public Boolean ifExistByTypeAndToken(TokenType type, String token){
        return tokenRepository.findByTypeAndToken(type, token).isPresent();
    }

    @Override
    public TokenEntity findByUserId(ObjectId userId) {
        return tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(env.getProperty("error.TokenIsNotValid")));
    }
    @Override
    public TokenEntity findByUserIdAndTokenType(ObjectId userId, TokenType tokenType) {
        return tokenRepository.findByUserIdAndType(userId, tokenType)
                .orElse(null);
    }

    private TokenEntity getByTypeAndToken(TokenType type, String token){
        return tokenRepository.findByTypeAndToken(type, token).orElse(null);
    }

    @Override
    public String generateAccessToken(UsernamePasswordDto usernamePassword) {

        User user = userService.findUserByNameOrEmail(usernamePassword.getName());
        CredentialsEntity credentialsEntity = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        validateUserPassword (usernamePassword.getPassword(), credentialsEntity.getPassword());
        ObjectId userId = credentialsEntity.getUserId();

        TokenEntity token =  createUserToken(userId, TokenType.ACCESS_TOKEN);
        return token.getToken();
    }

    @Override
    public String generateAccessTokenForAnonim(User user) {
        user = userService.find(user);
        TokenEntity token =  createUserToken(user.getId(), TokenType.ACCESS_TOKEN);
        return token.getToken();
    }
    private void validateUserPassword (String incomingPassword, String storedPassword) {
        if(!passwordEncoder.matches(incomingPassword, storedPassword)) throw new BadCredentialsException("Bad Credentials");
    }
}
