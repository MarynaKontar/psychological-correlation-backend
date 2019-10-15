package com.psycorp.service.implementation;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.mongodb.client.MongoCollection;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.security.token.AuthorisationToken;
import com.psycorp.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;

    private MongoCollection userCollection;
    private MongoCollection credentialsEntityCollection;
    private MongoCollection tokenEntityCollection;
    private MongoCollection valueCompatibilityAnswersEntityCollection;
    private MongoCollection userMatchEntityCollection;

    private long idConstant = 15478;
    private ObjectId id = new ObjectId(new Date(idConstant), 101);
    private ObjectId userId = new ObjectId(new Date(idConstant), 202);
    private ObjectId userId1, userId2, userId3, userId4, userId5, userId6, userId7, tokenEntityId;
    private User user, user1, user2, user3, user4, user5, user6, user7;
    private User userWithNullName, userWithNullAge, userWithNullGender;
    private TokenEntity tokenEntity;

    @BeforeEach
    void setUp() {
        userCollection = mongoTemplate.getCollection("user");
        credentialsEntityCollection = mongoTemplate.getCollection("credentialsEntity");
        tokenEntityCollection = mongoTemplate.getCollection("tokenEntity");
        valueCompatibilityAnswersEntityCollection = mongoTemplate.getCollection("valueCompatibilityAnswersEntity");
        userMatchEntityCollection = mongoTemplate.getCollection("userMatchEntity");
    }

    @AfterEach
    void tearDown(){
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(UserAccountEntity.class);
        mongoTemplate.dropCollection(CredentialsEntity.class);
        mongoTemplate.dropCollection(TokenEntity.class);
        mongoTemplate.dropCollection(ValueCompatibilityAnswersEntity.class);
        mongoTemplate.dropCollection(UserMatchEntity.class);
    }

    @Test
    void createAnonimUser() {

        User user = userService.createAnonimUser();

        assertTrue(user != null);

        Optional<User> savedUserOptional = userRepository.findById(user.getId());
        Optional<CredentialsEntity> savedCredentialsEntityOptional = credentialsRepository.findByUserId(user.getId());

        assertTrue(savedUserOptional.isPresent());
        assertTrue(savedCredentialsEntityOptional.isPresent());

        assertEquals(savedUserOptional.get().getRole(), UserRole.ANONIM);
        assertEquals(savedUserOptional.get().getId(), savedCredentialsEntityOptional.get().getUserId());
        assertNotEquals(savedUserOptional.get().getName(), null);
        assertEquals(savedUserOptional.get().getEmail(), null);
        assertEquals(savedUserOptional.get().getGender(), null);
        assertEquals(savedUserOptional.get().getAge(), null);
        assertEquals(savedCredentialsEntityOptional.get().getPassword(), null);
    }

    //  ========================= addNameAgeAndGender(User user) =========================
    @Test
    void addNameAgeAndGenderThrowsBadRequestExceptionForNotValidUser() {

        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(null);
        });
        assertEquals(exception.getMessage(), "User name, age or gender cant be null");


        Throwable nameException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullName);
        });
        assertEquals(nameException.getMessage(), "User name, age or gender cant be null");


        Throwable ageException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullAge);
        });
        assertEquals(ageException.getMessage(), "User name, age or gender cant be null");

        Throwable genderException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullGender);
        });
        assertEquals(genderException.getMessage(), "User name, age or gender cant be null");
    }

//    @Test
//    void addNameAgeAndGender() {
//        prepareUsersForTests();
//        prepareTokenEntityForTests();
////        User savedUser = ;
//        assertDoesNotThrow(() -> userRepository.save(user));
////        TokenEntity savedTokenEntity =;
//        assertDoesNotThrow(() -> tokenRepository.save(tokenEntity));
//
////        SecurityContext securityContext = SecurityContextHolder.getContext();
////        AuthorisationToken authorisationToken = AuthorisationToken.of(tokenEntity.getToken());
////        authorisationToken.setAuthenticated(true);
////        securityContext.setAuthentication(authorisationToken);
//
//
//        User changedUser = userService.addNameAgeAndGender(user4);
//
//        assertEquals(changedUser.getId(), user.getId());
//        // create user, save it ot db, check if it in db
//        // in tokenEntity will write token for principal user
//        //add name and gender
//        // check it
//
//    }


    private void prepareUsersForTests() {

        userId1 = new ObjectId(new Date(idConstant), 1);
        userId2 = new ObjectId(new Date(idConstant), 2);
        userId3 = new ObjectId(new Date(idConstant), 3);
        userId4 = new ObjectId(new Date(idConstant), 4);
        userId5 = new ObjectId(new Date(idConstant), 5);
        userId6 = new ObjectId(new Date(idConstant), 6);
        userId7 = new ObjectId(new Date(idConstant), 7);

        Fixture.of(User.class).addTemplate("user1", new Rule() {{
            add("id", userId1);
            add("name", "userName1");
            add("email", "email1@gmail.com");
            add("age", 45);
            add("gender", Gender.MALE);
            add("role", UserRole.USER);
            add("usersForMatchingId", Arrays.asList(userId));
        }});
        Fixture.of(User.class).addTemplate("user2", new Rule() {{
            add("id", userId2);
            add("name", "userName2");
            add("email", "email2@gmail.com");
            add("age", 38);
            add("gender", Gender.MALE);
            add("role", UserRole.USER);
        }});
        Fixture.of(User.class).addTemplate("user3", new Rule() {{
            add("id", userId3);
            add("name", "userName3");
            add("email", "email3@gmail.com");
            add("age", 27);
            add("gender", Gender.FEMALE);
            add("role", UserRole.USER);
            add("usersForMatchingId", Arrays.asList(userId));
        }});
        Fixture.of(User.class).addTemplate("addNameAgeAndGender", new Rule() {{
            add("id", userId);
            add("name", "userName4");
            add("age", 38);
            add("gender", Gender.FEMALE);
        }});


        Fixture.of(User.class).addTemplate("user", new Rule() {{
            add("id", userId);
//            add("name", "userName");
//            add("email", "email@gmail.com");
//            add("age", 25);
//            add("gender", Gender.FEMALE);
//            add("role", UserRole.USER);
//            add("usersForMatchingId", Arrays.asList(
//                    userId1,
//                    userId2,
//                    userId3));
        }});

        user = Fixture.from(User.class).gimme("user");
        user1 = Fixture.from(User.class).gimme("user1");
        user2 = Fixture.from(User.class).gimme("user2");
        user3 = Fixture.from(User.class).gimme("user3");
        user4 = Fixture.from(User.class).gimme("addNameAgeAndGender");
    }

    private void prepareUsersForAddNameAgeAndGenderMethod() {
        Fixture.of(User.class).addTemplate("userWithNullName", new Rule(){{
            add("name", null);
            add("age", 35);
            add("gender", Gender.FEMALE);

        }});
        Fixture.of(User.class).addTemplate("userWithNullAge", new Rule(){{
            add("name", "name");
            add("age", null);
            add("gender", Gender.FEMALE);

        }});
        Fixture.of(User.class).addTemplate("userWithNullGender", new Rule(){{
            add("name", "name");
            add("age", 35);
            add("gender", null);
        }});

        userWithNullName = Fixture.from(User.class).gimme("userWithNullName");
        userWithNullAge = Fixture.from(User.class).gimme("userWithNullAge");
        userWithNullGender = Fixture.from(User.class).gimme("userWithNullGender");
    }

    private void prepareTokenEntityForTests() {
        tokenEntityId = new ObjectId(new Date(idConstant), 7);
        Fixture.of(TokenEntity.class).addTemplate("tokenEntity1", new Rule() {{
            add("userId", userId);
            add("type", TokenType.ACCESS_TOKEN);
            add("token", "someToken");
        }});

        tokenEntity = Fixture.from(TokenEntity.class).gimme("tokenEntity1");
    }
}
