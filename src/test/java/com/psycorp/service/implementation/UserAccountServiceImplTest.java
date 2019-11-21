package com.psycorp.service.implementation;

import com.mongodb.MongoClientException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.security.TokenService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.psycorp.ObjectsForTests.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;


class UserAccountServiceImplTest {

    private long idConstant = 12345;
    private ObjectId id = new ObjectId(new Date(idConstant), 101);
    private ObjectId userId = new ObjectId(new Date(idConstant), 202);
    private ObjectId userId1, userId2, userId3, userId4, userId5, userId6, userId7, tokenEntityId;
    private User user, user1, user2, user3, user4, user5, user6, user7;
    private TokenEntity tokenEntity;
    private Optional<UserAccountEntity> userAccountEntityOptional;

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
    @Mock
    private Environment env;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        prepareUsersForTests();

//        MONGO_CONTAINER.start();
//        mongoDatabase = mongoClient.getDatabase("test");
//        mongoDatabase.createCollection("testCollection");
    }

    //  ======================================= insert(User user) ======================================================
    @Test
    void insertSuccess() {
        // given
        UserAccountEntity userAccountEntityWithoutListsOfIds = getShortUserAccountEntity(userId);

        when(userAccountRepository.insert(userAccountEntityWithoutListsOfIds))
                .thenReturn(userAccountEntityWithoutListsOfIds);

        // when
        UserAccountEntity userAccountEntity = userAccountService.insert(user);

        // then
        verify(userAccountRepository, times(1)).insert(userAccountEntityWithoutListsOfIds);
        assertEquals(userAccountEntity.getUserId(), userAccountEntityWithoutListsOfIds.getUserId());
        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);
    }

    @Test
    void insertThrowsBadRequestExceptionForNullUser() {
        // given
        when(env.getProperty(anyString())).thenReturn(anyString());
        // when
        assertThrows(BadRequestException.class, () -> {
            userAccountService.insert(null);
        });
        // then
        verify(env, times(1)).getProperty(anyString());
    }

    @Test
    void insertThrowsMongoExceptionForInsertionException() {
        doThrow(MongoClientException.class).when(userAccountRepository).insert(any(UserAccountEntity.class));

        // when
        assertThrows(MongoClientException.class, () -> {
            userAccountService.insert(user);
        });

    }

    //  ======================================= getUserAccount(User user) ==============================================
    @Test
    void getUserAccountSuccessForExistingUserAccountEntity() {
        // given
        tokenEntity = getTokenEntity(new ObjectId(new Date(idConstant), 8), userId2, TokenType.INVITE_TOKEN, "user2Token");
        userAccountEntityOptional = getUserAccountEntity(id, userId,
                Arrays.asList(userId4, userId5),
                Arrays.asList(userId6, userId7));
        when(userAccountRepository.findByUserId(userId)).thenReturn(userAccountEntityOptional);
        when(userService.findById(userId)).thenReturn(user);
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

        // when
        UserAccount userAccount = userAccountService.getUserAccount(user);

        // then
        verify(userAccountRepository, times(1)).findByUserId(userId);
        verify(userService, times(8)).findById(any(ObjectId.class));
        verify(valueCompatibilityAnswersService, times(4)).ifTestPassed(any(ObjectId.class));
        verify(tokenService, times(3)).findByUserIdAndTokenType(any(ObjectId.class), any(TokenType.class));
        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);

        assertEquals(user, userAccount.getUser());
        assertEquals(AccountType.OPEN, userAccount.getAccountType());
        assertEquals(true, userAccount.getIsValueCompatibilityTestPassed());

        assertEquals(1, userAccount.getInviteTokens().size());
        assertEquals(tokenEntity.getToken(), userAccount.getInviteTokens().get(0));

        assertEquals(2, userAccount.getUsersForMatching().size());
        assertEquals(user1, userAccount.getUsersForMatching().get(0));
        assertEquals(user3, userAccount.getUsersForMatching().get(1));

        assertEquals(2, userAccount.getUsersWhoInvitedYou().size());
        assertEquals(user4, userAccount.getUsersWhoInvitedYou().get(0));
        assertEquals(user5, userAccount.getUsersWhoInvitedYou().get(1));

        assertEquals(2, userAccount.getUsersWhoYouInvite().size());
        assertEquals(user6, userAccount.getUsersWhoYouInvite().get(0));
        assertEquals(user7, userAccount.getUsersWhoYouInvite().get(1));

    }

    @Test
    void getUserAccountThrowsNullPointerExceptionForNullUser() {
        // when
        assertThrows(NullPointerException.class, () -> {
            userAccountService.getUserAccount(null);
        });

        // then
        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);
    }

    @Test
    void getUserAccountForAnonimUser() {
        // given
        tokenEntity = getTokenEntity(new ObjectId(new Date(idConstant), 8), userId2, TokenType.INVITE_TOKEN, "user2Token");
        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userService.findById(userId1)).thenReturn(user1);
        when(userService.findById(userId2)).thenReturn(user2);
        when(userService.findById(userId3)).thenReturn(user3);
        when(valueCompatibilityAnswersService.ifTestPassed(any(ObjectId.class))).thenReturn(true);
        when(valueCompatibilityAnswersService.ifTestPassed(userId2)).thenReturn(false);
        when(tokenService.findByUserIdAndTokenType(userId2, TokenType.INVITE_TOKEN))
                .thenReturn(tokenEntity);

        // when
        UserAccount userAccount = userAccountService.getUserAccount(user);

        // then
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
        // given
        userAccountEntityOptional = getUserAccountEntity(id, userId,
                Arrays.asList(userId4, userId5),
                Arrays.asList(userId6, userId7));
        when(userAccountRepository.findByUserId(userId)).thenReturn(userAccountEntityOptional);
        doThrow(BadRequestException.class).when(userService).findById(userId);

        // when
        assertThrows(BadRequestException.class, () -> {
            userAccountService.getUserAccount(user);
        });

        // then
        verify(userAccountRepository, times(1)).findByUserId(userId);
        verify(userService, times(1)).findById(userId);
        verifyNoMoreInteractions(userAccountRepository, userService, valueCompatibilityAnswersService, tokenService);
    }

    //  ========================= getAllUserForMatchingPassedTest() ====================================================

    @Test
    @DisplayName("Test not yet implemented")
    void getAllUserForMatchingPassedTest() {
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

    // ============================================== private ==========================================================
    private void prepareUsersForTests() {
        userId1 = new ObjectId(new Date(idConstant), 1);
        userId2 = new ObjectId(new Date(idConstant), 2);
        userId3 = new ObjectId(new Date(idConstant), 3);
        userId4 = new ObjectId(new Date(idConstant), 4);
        userId5 = new ObjectId(new Date(idConstant), 5);
        userId6 = new ObjectId(new Date(idConstant), 6);
        userId7 = new ObjectId(new Date(idConstant), 7);

        user1 = getRegisteredUser(userId1, "userName1", Collections.singletonList(userId), "user1");
        user2 = getAnonimUser(userId2, "userName2", Collections.singletonList(userId), "user2");
        user3 = getRegisteredUser(userId3, "userName3", Collections.singletonList(userId), "user3");
        user4 = getRegisteredUser(userId4, "userName4", null, "user4");
        user5 = getRegisteredUser(userId5, "userName5", null, "user5");
        user6 = getRegisteredUser(userId6, "userName6", null, "user6");
        user7 = getRegisteredUser(userId7, "userName7", null, "user7");
        user = getRegisteredUser(userId, "userName", Arrays.asList(userId1, userId2, userId3), "fullUser");
    }

}