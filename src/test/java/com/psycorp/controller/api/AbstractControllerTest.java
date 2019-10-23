package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psycorp.FixtureObjectsForTest;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.service.implementation.CredentialsServiceImpl;
import com.psycorp.service.security.implementation.TokenServiceImpl;
import com.psycorp.сonverter.CredentialsDtoConverter;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.psycorp.FixtureObjectsForTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Astract class for test controllers and security layer.
 * Service layer, dto and server are mocked.
 * For server uses {@link MockMvc}.
 * Repository layer uses only for populate db before tests.
 * Use not embedded mongo database described in application-test.yml
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractControllerTest {
    final static String oldPassword = "oldPassword";
    final static String newPassword = "newPassword";
    Integer TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    CredentialsRepository credentialsRepository;
    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired
    ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    Environment env;


    @BeforeAll
    static void startUp() {
        fixtureAnonimUser();
        fixtureCredentialsDto();
        fixtureRegisteredUser();
    }

    /**
     * Checks if all collections is empty.
     */
    @BeforeEach
    void start() {
        assertEquals(mongoTemplate.findAll(User.class).size(), 0);
        assertEquals(mongoTemplate.findAll(CredentialsEntity.class).size(), 0);
        assertEquals(mongoTemplate.findAll(TokenEntity.class).size(), 0);
        assertEquals(mongoTemplate.findAll(ValueCompatibilityAnswersEntity.class).size(), 0);
        assertEquals(mongoTemplate.findAll(UserAccountEntity.class).size(), 0);
        assertEquals(mongoTemplate.findAll(UserMatchEntity.class).size(), 0);

        TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA = Integer.valueOf(env.getProperty("total.number.of.questions")); // 15
    }

    /**
     * Clears db after each test.
     */
    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(UserAccountEntity.class);
        mongoTemplate.dropCollection(CredentialsEntity.class);
        mongoTemplate.dropCollection(TokenEntity.class);
        mongoTemplate.dropCollection(ValueCompatibilityAnswersEntity.class);
        mongoTemplate.dropCollection(UserMatchEntity.class);
    }

    User populateDbWithRegisteredUser() {
        User user = Fixture.from(User.class).gimme("user");
        return userRepository.save(user);
    }

    User populateDbWithAnonimUser() {
        User user = Fixture.from(User.class).gimme("anonimUser");
        return userRepository.save(user);
    }

    TokenEntity populateDbWithTokenEntity(User user, TokenType tokenType, String token) {
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setType(tokenType);
        tokenEntity.setToken(token);
        tokenEntity.setUserId(user.getId());
        tokenEntity.setExpirationDate(LocalDateTime.now().plusDays(1));
        return tokenRepository.save(tokenEntity);
    }

    CredentialsEntity populateDbWithCredentialsEntity(User user, String password) {
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(user.getId());
        if(password != null) { credentialsEntity.setPassword(passwordEncoder.encode(password)); }
        return credentialsRepository.save(credentialsEntity);
    }

    UserAccountEntity populateDbWithUserAccountEntity(User user) {
        UserAccountEntity userAccountEntity = new UserAccountEntity();
        userAccountEntity.setUserId(user.getId());
        userAccountEntity.setAccountType(AccountType.OPEN);
        return userAccountRepository.insert(userAccountEntity);
    }

    Map<String, Object> populateDbWithAnonimUserAndCredentialsAndToken() {

        // add to db anonim user, credentialsEntity and tokenEntity for it
        User user = populateDbWithAnonimUser();
        CredentialsEntity credentialsEntity = populateDbWithCredentialsEntity(user, null);
        TokenEntity tokenEntity = populateDbWithTokenEntity(user, TokenType.ACCESS_TOKEN, "someTokenForAnonimUser");

        Map<String, Object> preparedObjects = new HashMap<>(3);
        preparedObjects.put("user", user);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        preparedObjects.put("tokenEntity", tokenEntity);

        return preparedObjects;
    }

    Map<String, Object> populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken() {
        User user = populateDbWithRegisteredUser();
        TokenEntity tokenEntity = populateDbWithTokenEntity(user, TokenType.ACCESS_TOKEN, "someTokenForRegisteredUser");
        CredentialsEntity credentialsEntity = populateDbWithCredentialsEntity(user, oldPassword);
        UserAccountEntity userAccountEntity = populateDbWithUserAccountEntity(user);

        Map<String, Object> preparedObjects = new HashMap<>(4);
        preparedObjects.put("user", user);
        preparedObjects.put("tokenEntity", tokenEntity);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        preparedObjects.put("userAccountEntity", userAccountEntity);
        return preparedObjects;
    }
}
