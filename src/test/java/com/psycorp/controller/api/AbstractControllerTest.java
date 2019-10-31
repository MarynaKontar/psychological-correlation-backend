package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psycorp.FixtureObjectsForTest;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.*;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.Area;
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
import org.bson.types.ObjectId;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        fixtureAnonimUser(null);
        fixtureCredentialsDto();
        fixtureRegisteredUser(null);
    }

    /**
     * Checks if all collections is empty.
     */
    @BeforeEach
    void start() {
        assertEquals(0, mongoTemplate.findAll(User.class).size());
        assertEquals(0, mongoTemplate.findAll(CredentialsEntity.class).size());
        assertEquals(0, mongoTemplate.findAll(TokenEntity.class).size());
        assertEquals(0, mongoTemplate.findAll(ValueCompatibilityAnswersEntity.class).size());
        assertEquals(0, mongoTemplate.findAll(UserAccountEntity.class).size());
        assertEquals(0, mongoTemplate.findAll(UserMatchEntity.class).size());

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

    User populateDbWithRegisteredUser(String name, Boolean isUsersForMatching) {
        if (name != null) { fixtureRegisteredUser(name); }
        User user = Fixture.from(User.class).gimme("user");

        if(isUsersForMatching) {
            ObjectId userId = new ObjectId();
            user.setId(userId);
            fixtureRegisteredUser("userForMatching1For" + name);
            User userForMatching1 = Fixture.from(User.class).gimme("user");
            userForMatching1.setUsersForMatchingId(Collections.singletonList(user.getId()));
            userForMatching1 = userRepository.save(userForMatching1);
            TokenEntity tokenEntityForUserForMatching1 = populateDbWithTokenEntity(userForMatching1, TokenType.ACCESS_TOKEN, "someTokenForUserForMatching1");

            fixtureRegisteredUser("userForMatching2For" + name);
            User userForMatching2 = Fixture.from(User.class).gimme("user");
            userForMatching2.setUsersForMatchingId(Collections.singletonList(user.getId()));
            userForMatching2 = userRepository.save(userForMatching2);
            TokenEntity tokenEntityForUserForMatching2 = populateDbWithTokenEntity(userForMatching2, TokenType.ACCESS_TOKEN, "someTokenForUserForMatching2");

            user.setUsersForMatchingId(Arrays.asList(userForMatching1.getId(), userForMatching2.getId()));
        }
        return userRepository.save(user);

    }

    User populateDbWithAnonimUser(String name) {
        if (name != null) { fixtureAnonimUser(name); }
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
        return populateDbWithAnonimUserAndCredentialsAndToken("anonimName", null);
    }

    Map<String, Object> populateDbWithAnonimUserAndCredentialsAndToken(String name, String token) {

        // add to db anonim user, credentialsEntity and tokenEntity for it
        User user = populateDbWithAnonimUser(name);
        CredentialsEntity credentialsEntity = populateDbWithCredentialsEntity(user, null);
        TokenEntity tokenEntity = populateDbWithTokenEntity(user, TokenType.ACCESS_TOKEN, token == null ? "someTokenForAnonimUser" : token);

        Map<String, Object> preparedObjects = new HashMap<>(3);
        preparedObjects.put("user", user);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        preparedObjects.put("tokenEntity", tokenEntity);

        return preparedObjects;
    }

    Map<String, Object> populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken(Boolean isUsersForMatching) {
        User user = populateDbWithRegisteredUser(null, isUsersForMatching);
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

    /**
     * Populates db with partial or full {@link ValueCompatibilityAnswersEntity} for given user to test entities that needs
     * ValueCompatibilityEntities in db with GOAL, or  GOAL and QUALITY (or GOAL and STATE), or full list of {@link Choice}.
     * Before calling this method, make sure that the list of choices contains only the data necessary for saving.
     * In this method, all data will be inserted into the database.
     * @param choices must not be {@literal null}.
     * @param user must not be {@literal null}.
     * @return new created {@link ValueCompatibilityAnswersEntity} with choices.
     */
    ValueCompatibilityAnswersEntity populateDbWithValueCompatibilityAnswersEntity(List<Choice> choices, User user, Area area) {
        ValueCompatibilityAnswersEntity answersEntity = new ValueCompatibilityAnswersEntity();
        answersEntity.setUserId(user.getId());
        switch (area) {
            case GOAL:
                answersEntity.setUserAnswers(choices
                        .stream()
                        .filter(choice -> choice.getArea() == Area.GOAL)
                        .collect(Collectors.toList()));
                break;
            case QUALITY:
                answersEntity.setUserAnswers(choices
                        .stream()
                        .filter(choice -> choice.getArea() == Area.GOAL ||
                                choice.getArea() == Area.QUALITY)
                        .collect(Collectors.toList()));
                break;
            case STATE:
                answersEntity.setUserAnswers(choices
                        .stream()
                        .filter(choice -> choice.getArea() == Area.GOAL ||
                                choice.getArea() == Area.STATE)
                        .collect(Collectors.toList()));
                break;
            case TOTAL:
                answersEntity.setUserAnswers(choices);
                break;
        }
        answersEntity.setPassDate(LocalDateTime.now());
        answersEntity.setCreationDate(LocalDateTime.now());

        if (area == Area.TOTAL) {
            answersEntity.setPassed(true);
        } else answersEntity.setPassed(false);

        return valueCompatibilityAnswersRepository.insert(answersEntity);
    }
}
