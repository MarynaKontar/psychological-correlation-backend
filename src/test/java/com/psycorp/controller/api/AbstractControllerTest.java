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
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserRepository;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.psycorp.FixtureObjectsForTest.fixtureAnonimUser;
import static com.psycorp.FixtureObjectsForTest.fixtureCredentialsDto;
import static com.psycorp.FixtureObjectsForTest.fixtureRegisteredUser;

/**
 * Astract class for test controllers and security layer.
 * Service layer, dto and server are mocked.
 * For server use {@link MockMvc}.
 * Repository layer uses only for populate db before tests.
 * Use not embedded mongo database described in application-test.yml
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractControllerTest {
    String oldPassword = "oldPassword";
    String newPassword = "newPassword";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    CredentialsServiceImpl credentialsService;
    @MockBean
    TokenServiceImpl tokenService;
    @MockBean
    CredentialsDtoConverter credentialsDtoConverter;
    @MockBean
    UserAccountDtoConverter userAccountDtoConverter;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper mapper;

    @BeforeAll
    static void startUp() {
        fixtureAnonimUser();
        fixtureCredentialsDto();
        fixtureRegisteredUser();
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

    Map<String, Object> prepareObjectsForSuccessfulRegistrationTest() {
        // populate db with user, tokenEntity and credentialsEntity
//        fixtureAnonimUser();
        User user = Fixture.from(User.class).gimme("anonimUser");
        user = userRepository.save(user);

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setType(TokenType.ACCESS_TOKEN);
        tokenEntity.setToken("someToken");
        tokenEntity.setUserId(user.getId());
        tokenEntity.setExpirationDate(LocalDateTime.now().plusDays(1));
        tokenEntity = tokenRepository.save(tokenEntity);

        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(user.getId());
        credentialsEntity = credentialsRepository.save(credentialsEntity);

        // prepare objects for successful registration test
//        fixtureCredentialsDto();
        CredentialsDto credentialsDto = Fixture.from(CredentialsDto.class).gimme("credentialsRegistrationDto");

        user.setAge(credentialsDto.getAge());
        user.setGender(credentialsDto.getGender());
        user.setName(credentialsDto.getName());
        user.setEmail(credentialsDto.getEmail());
        user.setRole(UserRole.USER);

        UserAccount userAccount = new UserAccount();
        userAccount.setUser(user);

        UserAccountDto userAccountDto = new UserAccountDto();
        SimpleUserDto simpleUserDto = new SimpleUserDto();
        simpleUserDto.setId(user.getId());
        simpleUserDto.setName(user.getName());
        simpleUserDto.setEmail(user.getEmail());
        simpleUserDto.setGender(user.getGender());
        simpleUserDto.setAge(user.getAge());
        userAccountDto.setUser(simpleUserDto);

        Map<String, Object> preparedObjects = new HashMap<>(6);
        preparedObjects.put("user", user);
        preparedObjects.put("tokenEntity", tokenEntity);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        preparedObjects.put("credentialsDto", credentialsDto);
        preparedObjects.put("userAccount", userAccount);
        preparedObjects.put("userAccountDto", userAccountDto);
        return preparedObjects;
    }

    Map<String, Object> populateDbForChangePasswordTest() {
        // populate db with user, tokenEntity and credentialsEntity
//        fixtureRegisteredUser();
        User user = Fixture.from(User.class).gimme("user");
        user = userRepository.save(user);

        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setType(TokenType.ACCESS_TOKEN);
        tokenEntity.setToken("someToken");
        tokenEntity.setUserId(user.getId());
        tokenEntity.setExpirationDate(LocalDateTime.now().plusDays(1));
        tokenEntity = tokenRepository.save(tokenEntity);

        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(user.getId());
        credentialsEntity.setPassword(passwordEncoder.encode(oldPassword));
        credentialsEntity = credentialsRepository.save(credentialsEntity);

        Map<String, Object> preparedObjects = new HashMap<>(3);
        preparedObjects.put("user", user);
        preparedObjects.put("tokenEntity", tokenEntity);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        return preparedObjects;
    }

}
