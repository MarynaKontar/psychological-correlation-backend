package com.psycorp.service.implementation;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
//import com.psycorp.AbstractIntegrationTest;
import com.psycorp.Application;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.security.TokenService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.bson.Document.parse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;


class UserAccountServiceImplTest
//        extends AbstractIntegrationTest
{
    private long idConstant = 15478;
    private ObjectId id = new ObjectId(new Date(idConstant), 101);
    private ObjectId userId = new ObjectId(new Date(idConstant), 202);
    private ObjectId userId1, userId2, userId3, userId4, userId5, userId6, userId7, tokenEntityId;
    private User user, user1, user2, user3, user4, user5, user6, user7;
    private TokenEntity tokenEntity;
    private Optional<UserAccountEntity> userAccountEntityOptional;
    private UserAccountEntity userAccountEntityForInsertion;

//    {
//        prepareUsersForTests();
//        prepareUserAccountEntityForTests();
//        prepareTokenEntityForTests();
//    }

    private MockMvc mockMvc;
    @InjectMocks
    private UserAccountServiceImpl userAccountService;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    @Mock
    private TokenService tokenService;
    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        prepareUsersForTests();
        prepareUserAccountEntityForTests();
        prepareTokenEntityForTests();

//        MONGO_CONTAINER.start();
//        mongoDatabase = mongoClient.getDatabase("test");
//        mongoDatabase.createCollection("testCollection");
    }

    //  ========================= insert(User user) =========================
    @Test
    public void insert() {

        prepareUserAccountEntityForInsertion();

        when(userAccountRepository.insert(userAccountEntityOptional.get()))
                .thenReturn(userAccountEntityOptional.get());

        userAccountService.insert(user);

        verify(userAccountRepository, times(1)).insert(userAccountEntityForInsertion);
        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);
    }


    //  ========================= getUserAccount(User user) =========================
    @Test
    void getUserAccountForExistingUserAccountEntity() {

        when(userService.findById(userId)).thenReturn(user);
        when(userAccountRepository.findByUserId(userId)).thenReturn(userAccountEntityOptional);
        when(valueCompatibilityAnswersService.ifTestPassed(any(ObjectId.class))).thenReturn(true);
        when(valueCompatibilityAnswersService.ifTestPassed(userId2)).thenReturn(false);
        when(userService.findById(userId1)).thenReturn(user1);
        when(userService.findById(userId2)).thenReturn(user2);
        when(userService.findById(userId3)).thenReturn(user3);
        when(userService.findById(userId4)).thenReturn(user4);
        when(userService.findById(userId5)).thenReturn(user5);
        when(userService.findById(userId6)).thenReturn(user6);
        when(userService.findById(userId7)).thenReturn(user7);
        when(tokenService.findByUserIdAndTokenType(userId2, TokenType.INVITE_TOKEN))
                .thenReturn(tokenEntity);


        UserAccount userAccount = userAccountService.getUserAccount(user);


        verify(userAccountRepository, times(1)).findByUserId(userId);
        verify(userService, times(8)).findById(any(ObjectId.class));
        verify(valueCompatibilityAnswersService, times(4)).ifTestPassed(any(ObjectId.class));
        verify(tokenService, times(3)).findByUserIdAndTokenType(any(ObjectId.class), any(TokenType.class));
        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);

        assertEquals(userAccount.getUser(), user);
        assertEquals(userAccount.getAccountType(), AccountType.OPEN);
        assertEquals(userAccount.getIsValueCompatibilityTestPassed(), true);

        assertEquals(userAccount.getInviteTokens().size(), 1);
        assertEquals(userAccount.getInviteTokens().get(0), tokenEntity.getToken());

        assertEquals(userAccount.getUsersForMatching().size(), 2);
        assertEquals(userAccount.getUsersForMatching().get(0), user1);
        assertEquals(userAccount.getUsersForMatching().get(1), user3);

        assertEquals(userAccount.getUsersWhoInvitedYou().size(), 2);
        assertEquals(userAccount.getUsersWhoInvitedYou().get(0), user4);
        assertEquals(userAccount.getUsersWhoInvitedYou().get(1), user5);

        assertEquals(userAccount.getUsersWhoYouInvite().size(), 2);
        assertEquals(userAccount.getUsersWhoYouInvite().get(0), user6);
        assertEquals(userAccount.getUsersWhoYouInvite().get(1), user7);

    }

    @Test
    void getUserAccountThrowsNullPointerExceptionForNullUser() {

        assertThrows(NullPointerException.class, () -> {
            userAccountService.getUserAccount(null);
        });

        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);
    }

    @Test
    void getUserAccountForAnonimUser() {

        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userService.findById(userId1)).thenReturn(user1);
        when(userService.findById(userId2)).thenReturn(user2);
        when(userService.findById(userId3)).thenReturn(user3);
        when(valueCompatibilityAnswersService.ifTestPassed(any(ObjectId.class))).thenReturn(true);
        when(valueCompatibilityAnswersService.ifTestPassed(userId2)).thenReturn(false);
        when(tokenService.findByUserIdAndTokenType(userId2, TokenType.INVITE_TOKEN))
                .thenReturn(tokenEntity);


        UserAccount userAccount = userAccountService.getUserAccount(user);


        verify(userAccountRepository, times(1)).findByUserId(userId);
        verify(userService, times(3)).findById(any(ObjectId.class));
        verify(valueCompatibilityAnswersService, times(4)).ifTestPassed(any(ObjectId.class));
        verify(tokenService, times(3)).findByUserIdAndTokenType(any(ObjectId.class), any(TokenType.class));
        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);

        assertEquals(userAccount.getUser(), user);
        assertEquals(userAccount.getAccountType(), AccountType.OPEN);
        assertEquals(userAccount.getIsValueCompatibilityTestPassed(), true);

        assertEquals(userAccount.getInviteTokens().size(), 1);
        assertEquals(userAccount.getInviteTokens().get(0), tokenEntity.getToken());

        assertEquals(userAccount.getUsersForMatching().size(), 2);
        assertEquals(userAccount.getUsersForMatching().get(0), user1);
        assertEquals(userAccount.getUsersForMatching().get(1), user3);

        assertEquals(userAccount.getUsersWhoYouInvite(), null);
        assertEquals(userAccount.getUsersWhoInvitedYou(), null);
    }

    @Test
    void getUserAccountThrowsBadRequestExceptionForNotExistingUser() {

        when(userAccountRepository.findByUserId(userId)).thenReturn(userAccountEntityOptional);
        doThrow(BadRequestException.class).when(userService).findById(userId);

        assertThrows(BadRequestException.class, () -> {
            userAccountService.getUserAccount(user);
        });

        verify(userAccountRepository, times(1)).findByUserId(userId);
        verify(userService, times(1)).findById(userId);
        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);
    }

    //  ========================= getAllUserForMatchingPassedTest() =========================

    @Test
    void getAllUserForMatchingPassedTest() {
//        mongoOperations.insert("{\"df\": \"fgh\"}");
//        MongoDatabase mongoDatabase = mongoClient.getDatabase("test");
//        mongoDatabase.createCollection("testCollection");
//        MongoCollection<Document> collection = mongoDatabase.getCollection("testCollection");
//        collection.insertOne(new Document(parse("{\"fg\":\"yyy\", \"ghhjj\":\"yyy\"}")));
//        System.out.println(collection.find().first().toJson());
    }







    @Test
    @DisplayName("Test not yet implemented")
    void getAllRegisteredAndPassedTestPageable() {
    }

    @DisplayName("Test not yet implemented")
    @Test
    void getUserAccountEntityByUserIdOrNull() {
    }

    @DisplayName("Test not yet implemented")
    @Test
    void getUsersForMatching() {
    }

    @DisplayName("Test not yet implemented")
    @Test
    void update() {
    }

    @DisplayName("Test not yet implemented")
    @Test
    void inviteForMatching() {
    }

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
        Fixture.of(User.class).addTemplate("user4", new Rule() {{
            add("id", userId4);
            add("name", "userName4");
            add("email", "email4@gmail.com");
            add("age", 38);
            add("gender", Gender.FEMALE);
            add("role", UserRole.USER);
        }});
        Fixture.of(User.class).addTemplate("user5", new Rule() {{
            add("id", userId5);
            add("name", "userName5");
            add("email", "email5@gmail.com");
            add("age", 56);
            add("gender", Gender.MALE);
            add("role", UserRole.USER);
        }});
        Fixture.of(User.class).addTemplate("user6", new Rule() {{
            add("id", userId6);
            add("name", "userName6");
            add("email", "email6@gmail.com");
            add("age", 23);
            add("gender", Gender.FEMALE);
            add("role", UserRole.USER);
        }});
        Fixture.of(User.class).addTemplate("user7", new Rule() {{
            add("id", userId7);
            add("name", "userName7");
            add("email", "email7@gmail.com");
            add("age", 43);
            add("gender", Gender.MALE);
            add("role", UserRole.USER);
        }});

        Fixture.of(User.class).addTemplate("fullUser", new Rule() {{
            add("id", userId);
            add("name", "userName");
            add("email", "email@gmail.com");
            add("age", 25);
            add("gender", Gender.FEMALE);
            add("role", UserRole.USER);
            add("usersForMatchingId", Arrays.asList(
                    userId1,
                    userId2,
                    userId3));
        }});

        user = Fixture.from(User.class).gimme("fullUser");
        user1 = Fixture.from(User.class).gimme("user1");
        user2 = Fixture.from(User.class).gimme("user2");
        user3 = Fixture.from(User.class).gimme("user3");
        user4 = Fixture.from(User.class).gimme("user4");
        user5 = Fixture.from(User.class).gimme("user5");
        user6 = Fixture.from(User.class).gimme("user6");
        user7 = Fixture.from(User.class).gimme("user7");
    }

    private void prepareUserAccountEntityForTests() {
        Fixture.of(UserAccountEntity.class).addTemplate("valid", new Rule() {{
            add("id", id);
            add("userId", userId);
            add("accountType", AccountType.OPEN);
            add("usersWhoInvitedYouId", Arrays.asList(
                    userId4,
                    userId5));
            add("usersWhoYouInviteId", Arrays.asList(
                    userId6,
                    userId7));
        }});

        userAccountEntityOptional = Optional.of(Fixture.from(UserAccountEntity.class).gimme("valid"));
    }

    private void prepareTokenEntityForTests() {
        tokenEntityId = new ObjectId(new Date(idConstant), 7);
        Fixture.of(TokenEntity.class).addTemplate("tokenEntity1", new Rule() {{
            add("id", tokenEntityId);
            add("userId", userId2);
            add("type", TokenType.INVITE_TOKEN);
            add("token", "someToken");
            add("expirationDate", LocalDateTime.MAX);
        }});

        tokenEntity = Fixture.from(TokenEntity.class).gimme("tokenEntity1");
    }

    private void prepareUserAccountEntityForInsertion() {
        Fixture.of(UserAccountEntity.class).addTemplate("insert", new Rule() {{
            add("userId", userId);
            add("accountType", AccountType.OPEN);
        }});

        userAccountEntityForInsertion = Fixture.from(UserAccountEntity.class).gimme("insert");
    }
}