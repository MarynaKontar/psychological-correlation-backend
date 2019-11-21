package com.psycorp.service.implementation;

import com.mongodb.MongoClientException;
import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.*;
import java.util.stream.Collectors;

import static com.psycorp.ObjectsForTests.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CredentialsRepository credentialsRepository;
    @Mock
    private ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    @Mock
    private AuthService authService;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private UserMatchRepository userMatchRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private MongoOperations mongoOperations;
    @Mock
    private Environment env;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
//        prepareUsersForTests();

    }

    @AfterEach
    void tearDown() {
        // !!!!!!! DELETE because it takes up a lot of resources
        verifyNoMoreInteractions(userRepository, tokenRepository, userAccountRepository, userMatchRepository, credentialsRepository, mongoOperations, env, authService, valueCompatibilityAnswersRepository);
    }


    //  ============================================== createAnonimUser() ==============================================
    @Test
    void createAnonimUserSuccess() {
        //given
        User user = getAnonimUser();
        CredentialsEntity credentialsEntity = getCredentialsEntity(user, "password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(credentialsRepository.save(any(CredentialsEntity.class))).thenReturn(credentialsEntity);

        // when
        User createdUser = userService.createAnonimUser();

        // then
        assertTrue(createdUser != null);

        assertEquals(createdUser.getRole(), UserRole.ANONIM);
        assertNotNull(createdUser.getName());
        assertNull(createdUser.getEmail());
        assertNull(createdUser.getGender());
        assertNull(createdUser.getAge());
        assertNull(createdUser.getUsersForMatchingId());
        verify(userRepository, times(1)).save(any(User.class));
        verify(credentialsRepository, times(1)).save(any(CredentialsEntity.class));
    }


    //  ======================================== addNameAgeAndGender(User user) ========================================
    @Test
    void addNameAgeAndGenderSuccess() {
        // given
        User user = getAnonimUser();
        user.setId(new ObjectId());
        TokenPrincipal tokenPrincipal = getTokenPrincipal(user);
        User userWithNameAgeAndGender = getIncompleteUser();
        when(authService.getAuthPrincipal()).thenReturn(tokenPrincipal);
        when(userRepository.findById(tokenPrincipal.getId())).thenReturn(Optional.of(user));
        when(userRepository.findUserByNameOrEmail(userWithNameAgeAndGender.getName(), userWithNameAgeAndGender.getName()))
                .thenReturn(Optional.empty());

        userWithNameAgeAndGender.setId(user.getId());
        userWithNameAgeAndGender.setRole(user.getRole());
        Update updateUser = new Update()
                .set("name", (userWithNameAgeAndGender.getName()))
                .set("age", (userWithNameAgeAndGender.getAge()))
                .set("gender", (userWithNameAgeAndGender.getGender()));
        when(mongoOperations.findAndModify(any(Query.class), eq(updateUser),
                any(FindAndModifyOptions.class), eq(User.class))).thenReturn(userWithNameAgeAndGender);

        // when
        User changedUser = userService.addNameAgeAndGender(userWithNameAgeAndGender);

        // then
        assertEquals(userWithNameAgeAndGender, changedUser);
        verify(userRepository).findById(tokenPrincipal.getId());
        verify(userRepository).findUserByNameOrEmail(userWithNameAgeAndGender.getName(), userWithNameAgeAndGender.getName());
        verify(authService).getAuthPrincipal();
        verify(mongoOperations).findAndModify(any(Query.class), eq(updateUser)
                , any(FindAndModifyOptions.class), eq(User.class));
    }

    @Test
    void addNameAgeAndGenderThrowsBadRequestExceptionForNotValidUser() {
        // given
        User userWithNullName = getNotValidIncompleteUser(UserField.NAME);
        User userWithNullGender = getNotValidIncompleteUser(UserField.GENDER);
        User userWithNullAge = getNotValidIncompleteUser(UserField.AGE);
        String exceptionMessage = "User name, age or gender cant be null.";
        when(env.getProperty("error.UserNameAgeOrGenderCantBeNull")).thenReturn(exceptionMessage);

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(null);
        });
        // then
        assertEquals(exceptionMessage, exception.getMessage());

        // when
        Throwable nameException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullName);
        });
        // then
        assertEquals(exceptionMessage, nameException.getMessage());

        // when
        Throwable ageException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullAge);
        });
        // then
        assertEquals(exceptionMessage, ageException.getMessage());

        // when
        Throwable genderException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullGender);
        });
        // then
        assertEquals(exceptionMessage, genderException.getMessage());

        verify(env, times(4)).getProperty("error.UserNameAgeOrGenderCantBeNull");
    }


    // =========== addNewUsersForMatching(User user, List<User> usersForMatching, Update.Position position) ============
    @Test
    void addNewUsersForMatchingSuccess() {
        // given
        User user = getAnonimUser();
        User userForMatching1 = getAnonimUser(new ObjectId(), "userForMatchingId1", null, "user1");
        User userForMatching2 = getAnonimUser(new ObjectId(), "userForMatchingId2", null, "user2");
        User userForMatching3 = getAnonimUser(new ObjectId(), "userForMatchingId3", null, "user3");
        List<User> usersForMatching = Arrays.asList(userForMatching1, userForMatching2, userForMatching3);

        Set<ObjectId> usersForMatchingId = usersForMatching.stream().map(User::getId).collect(Collectors.toSet());
        User updatedUser = new User();
        updatedUser.setName(user.getName());
        updatedUser.setRole(user.getRole());
        updatedUser.setId(user.getId());
        updatedUser.setUsersForMatchingId(new ArrayList<ObjectId>(usersForMatchingId));

        Update update = new Update();
        update
                .push("usersForMatchingId")
                .atPosition(Update.Position.LAST)
                .each(usersForMatchingId);
        when(mongoOperations.findAndModify(any(Query.class), eq(update),
                any(FindAndModifyOptions.class), eq(User.class))).thenReturn(updatedUser);

        // when
        User changedUser = userService.addNewUsersForMatching(user, usersForMatching, Update.Position.LAST);

        // then
        assertEquals(3, changedUser.getUsersForMatchingId().size());
        assertTrue(usersForMatching
                .stream()
                .allMatch(userForMatching -> changedUser.getUsersForMatchingId().contains(userForMatching.getId())));
        verify(mongoOperations).findAndModify(any(Query.class), eq(update),
                any(FindAndModifyOptions.class), eq(User.class));
    }

    @Test
    void addNewUsersForMatchingThrowsNullPointerExceptionForNullUsersForMatching() {
        // given
        User user = getAnonimUser();

        // when
        assertThrows(NullPointerException.class, () ->
                userService.addNewUsersForMatching(user, null, Update.Position.LAST));
    }


    // ================================================ find(User user) ================================================
    @Test
    void findSuccess() {
        // given
        User user = getRegisteredUser(new ObjectId(), "name", null, "user");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        User foundUser = userService.find(user);

        // then
        assertEquals(user, foundUser);
        verify(userRepository).findById(user.getId());
    }

    @Test
    void findThrowsExceptionForNullUser() {
        //given
        String exceptionMessage = "User or userId can't be null.";
        when(env.getProperty("error.UserOrUserIdCan`tBeNull")).thenReturn(exceptionMessage);

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.find(null);
        });

        // then
        assertEquals(exceptionMessage, exception.getMessage());
        verify(env).getProperty("error.UserOrUserIdCan`tBeNull");
    }

    @Test
    void findThrowsExceptionForNullUserId() {
        // given
        User user = new User();
        user.setId(null);
        String exceptionMessage = "User or userId can't be null.";
        when(env.getProperty("error.UserOrUserIdCan`tBeNull")).thenReturn(exceptionMessage);

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.find(user);
        });

        // then
        assertEquals(exceptionMessage, exception.getMessage());
        verify(env).getProperty("error.UserOrUserIdCan`tBeNull");
    }

    @Test
    void findThrowsExceptionForNotExistingUserId() {
        // given
        User notExistingUser = new User();
        notExistingUser.setId(new ObjectId());
        when(userRepository.findById(notExistingUser.getId())).thenReturn(Optional.empty());
        when(env.getProperty("error.noUserFound")).thenReturn("There isn't user");

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.find(notExistingUser);
        });

        // then
        assertTrue(exception.getMessage().contains(env.getProperty("error.noUserFound")));
        verify(userRepository).findById(notExistingUser.getId());
        verify(env, times(2)).getProperty("error.noUserFound");
    }


    // ===================================== findById(ObjectId userId) =================================================

    @Test
    void findByIdSuccess() {
        // given
        User user = getRegisteredUser(new ObjectId(), "name", null, "user");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        User foundUser = userService.findById(user.getId());

        // then
        assertEquals(user, foundUser);
        verify(userRepository).findById(user.getId());
    }

    @Test
    void findByIdThrowsExceptionForNullUserId() {
        //given
        doThrow(IllegalArgumentException.class).when(userRepository).findById(null);
        // when
        assertThrows(IllegalArgumentException.class, () -> {
            userService.findById(null);
        });
        verify(userRepository).findById(null);
    }

    @Test
    void findByIdThrowsExceptionForNotExistingUserId() {
        // given
        ObjectId notExistingUserId = new ObjectId();
        String exceptionMessage = "There isn't user";
        when(userRepository.findById(notExistingUserId)).thenReturn(Optional.empty());
        when(env.getProperty("error.noUserFound")).thenReturn(exceptionMessage);

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.findById(notExistingUserId);
        });

        // then
        assertTrue(exception.getMessage().contains(exceptionMessage));
        verify(userRepository).findById(notExistingUserId);
        verify(env).getProperty("error.noUserFound");
    }


    // ============================= findUserByNameOrEmail(String nameOrEmail) =========================================

    @Test
    void findByNameOrEmailSuccess() {
        // given
        User user = getRegisteredUser(new ObjectId(), "name", null, "user");
        when(userRepository.findUserByNameOrEmail(user.getName(), user.getName())).thenReturn(Optional.of(user));
        when(userRepository.findUserByNameOrEmail(user.getEmail(), user.getEmail())).thenReturn(Optional.of(user));

        // NAME
        // when
        User foundUser = userService.findUserByNameOrEmail(user.getName());
        // then
        assertEquals(user, foundUser);

        // EMAIL
        // when
        foundUser = userService.findUserByNameOrEmail(user.getEmail());
        // then
        assertEquals(user, foundUser);

        verify(userRepository).findUserByNameOrEmail(user.getName(), user.getName());
        verify(userRepository).findUserByNameOrEmail(user.getEmail(), user.getEmail());
    }

    @Test
    void findByNameOrEmailThrowsExceptionForNullNameOrEmail() {
        // given
        String exceptionMessage = "User name or email can't be null.";
        when(env.getProperty("error.UserNameOrEmailCan`tBeNull")).thenReturn(exceptionMessage);

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.findUserByNameOrEmail(null);
        });

        // then
        assertEquals(exceptionMessage, exception.getMessage());
        verify(env).getProperty("error.UserNameOrEmailCan`tBeNull");
    }

    @Test
    void findByNameOrEmailThrowsExceptionForNotExistingName() {
        // given
        String notExistingName = "notExistingName";
        String exceptionMessage = "There isn't user";
        when(userRepository.findUserByNameOrEmail(notExistingName, notExistingName)).thenReturn(Optional.empty());
        when(env.getProperty("error.noUserFound")).thenReturn(exceptionMessage);

        // when
        Throwable nameException = assertThrows(BadRequestException.class, () -> {
            userService.findUserByNameOrEmail(notExistingName);
        });

        // then
        assertTrue(nameException.getMessage().contains(exceptionMessage));
        verify(userRepository).findUserByNameOrEmail(notExistingName, notExistingName);
        verify(env, times(1)).getProperty("error.noUserFound");
    }

    @Test
    void findByNameOrEmailThrowsExceptionForNotExistingEmail() {
        // given
        String notExistingEmail = "notExistingEmail@gmail.com";
        String exceptionMessage = "There isn't user";
        when(userRepository.findUserByNameOrEmail(notExistingEmail, notExistingEmail)).thenReturn(Optional.empty());
        when(env.getProperty("error.noUserFound")).thenReturn(exceptionMessage);

        // when
        Throwable emailException = assertThrows(BadRequestException.class, () -> {
            userService.findUserByNameOrEmail(notExistingEmail);
        });
        // then
        assertTrue(emailException.getMessage().contains(exceptionMessage));
        verify(userRepository).findUserByNameOrEmail(notExistingEmail, notExistingEmail);
        verify(env, times(1)).getProperty("error.noUserFound");
    }


    // ========================================= getPrincipalUser() ====================================================
    @Test
    void getPrincipalUserSuccess() {
        // given
        User user = getAnonimUser();
        user.setId(new ObjectId());
        TokenPrincipal tokenPrincipal = getTokenPrincipal(user);
        when(authService.getAuthPrincipal()).thenReturn(tokenPrincipal);
        when(userRepository.findById(tokenPrincipal.getId())).thenReturn(Optional.of(user));

        // when
        User principalUser = userService.getPrincipalUser();

        // then
        assertEquals(user, principalUser);
        verify(authService).getAuthPrincipal();
        verify(userRepository).findById(principalUser.getId());
    }

//    @Test
//    void getPrincipalUserThrowsAuthorizationExceptionForNotAuthorisedUser() {
//        // given
//        given(authService.getAuthPrincipal()).willReturn(null);
//
//        // when
//        Throwable exception = assertThrows(AuthorizationException.class, () -> {
//            userService.getPrincipalUser();
//        });
//
//        // then
//        AuthorizationException authorizationException = (AuthorizationException) exception;
//        assertEquals(env.getProperty("error.UserNotAuthorised"), exception.getMessage());
//        assertEquals(ErrorEnum.NOT_AUTHORIZED, authorizationException.getError());
//    }


    @Test
    void checkIfUsernameOrEmailExist() {
    }

    @Test
    void checkIfExistById() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }
}