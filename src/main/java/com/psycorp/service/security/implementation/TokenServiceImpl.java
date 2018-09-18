package com.psycorp.service.security.implementation;

import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.service.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;

    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
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
