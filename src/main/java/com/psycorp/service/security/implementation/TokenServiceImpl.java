package com.psycorp.service.security.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final UserService userService;
    private final AuthService authService;
    private final MongoOperations mongoOperations;
    private final Environment env;

    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository, UserService userService, AuthService authService, MongoOperations mongoOperations, Environment env) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.authService = authService;
        this.mongoOperations = mongoOperations;
        this.env = env;
    }

//    private JwtService jwtService;

    @Override
    public TokenEntity createUserToken(User user, TokenType tokenType) {
        String token = UUID.randomUUID().toString();

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setUser(user);
        tokenEntity.setType(tokenType);
        tokenEntity.setExpirationDate(LocalDateTime.now().plusDays(7));

        tokenRepository.save(tokenEntity);

        return tokenEntity;
    }

    @Override
    @Transactional
    public List<String> generateTokenList(Integer n){ //n tokens
        User principal = getUser();
        List<User> usersForMatching = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            usersForMatching.add(userService.createAnonimUser());
        }
        //TODO добавить usersForMatchingи в создаваемых пользователей

        List<String> tokens= new ArrayList<>(n);
        usersForMatching.forEach(user -> tokens.add(createUserToken(user,TokenType.ACCESS_TOKEN).getToken()));

        // UPDATE USERSFORMATCHING
        Update updateUser = new Update().push("usersForMatching").each(usersForMatching);
        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(principal.getId()));
        mongoOperations.updateFirst(query, updateUser, User.class);

       return tokens;
    }

    private User getUser() {

        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();
        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть токен
           return userService.findById(tokenPrincipal.getId()); // и для него есть пользователь, то берем этого пользователя
        } else { throw new AuthorizationException("User not authorised", ErrorEnum.NOT_AUTHORIZED); } // если токен == null или у него id == null
    }

    @Override
    public User getUserByToken(String token){
        // TODO решить, что делать, если токена нет или истек срок
        TokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthorizationException("", ErrorEnum.TOKEN_EXPIRED));
       return userService.findById(tokenEntity.getUser().getId());
    }

//    @Override
//    public void updateUserToken(String token, LocalDateTime lastUsed) {
//        TokenEntity tokenEntity = tokenRepository.findByToken(token).get();
//        tokenRepository.save(new TokenEntity(tokenEntity.getId(), tokenEntity.getType(), lastUsed,
//                tokenEntity.getToken(), tokenEntity.getUser()));
//    }
//
//    @Override
//    public void removeUserTokens(ObjectId user_id) {
//        TokenEntity token = tokenRepository.findByUser_Id(user_id).orElseThrow(() -> new RuntimeException("Invalid Token"));
//            tokenRepository.delete(token);
//    }

//    @Override
//    public String createJwtToken() {
//        Map<String, Object> data = new HashMap<>();
//        data.put("id", "someId");
//        data.put("role", "DEVICE");
//
//        return jwtService.generateToken(data);
//    }
}