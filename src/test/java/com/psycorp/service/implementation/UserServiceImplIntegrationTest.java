package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static com.psycorp.ObjectsForTests.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

/**
 * Integration tests for {@link UserServiceImpl}.
 * Getting principal user is mocked.
 * Use not embedded mongo database described in application-test.yml
 */
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplIntegrationTest extends AbstractServiceIntegrationTest{

    @Autowired
    private UserService userService;

    @MockBean
    private AuthService authService;


    //  ============================================== createAnonimUser() ==============================================
    @Test
    void createAnonimUserSuccess() {

        // when
        User user = userService.createAnonimUser();

        // then
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

    //  ======================================== addNameAgeAndGender(User user) ========================================
    @Test
    void addNameAgeAndGenderToAnonimUserSuccess() {
        // given
        User user = populateDb.populateDbWithAnonimUser("anonimUser");
        TokenPrincipal tokenPrincipal = getTokenPrincipal(user);
        User userWithNameAgeAndGender = getIncompleteUser();

        // mocked security: getting principal user is mocked
        given(authService.getAuthPrincipal()).willReturn(tokenPrincipal);

        // when
        User changedUser = userService.addNameAgeAndGender(userWithNameAgeAndGender);

        // then
        assertEquals(1, userRepository.findAll().size());
        assertEquals(user.getId(), userRepository.findAll().get(0).getId());
        assertEquals(user.getId(), changedUser.getId());
        assertEquals(userWithNameAgeAndGender.getName(), changedUser.getName());
        assertEquals(userWithNameAgeAndGender.getGender(), changedUser.getGender());
        assertEquals(userWithNameAgeAndGender.getAge(), changedUser.getAge());
        assertEquals(null, changedUser.getEmail());
    }

    @Test
    void addNameAgeAndGenderToRegisteredUserSuccess() {
        // given
        User user = populateDb.populateDbWithRegisteredUser("user", false);
        TokenPrincipal tokenPrincipal = getTokenPrincipal(user);
        User userWithNameAgeAndGender = getIncompleteUser();

        // mocked security: getting principal user is mocked
        given(authService.getAuthPrincipal()).willReturn(tokenPrincipal);

        // when
        User changedUser = userService.addNameAgeAndGender(userWithNameAgeAndGender);

        // then
        assertEquals(1, userRepository.findAll().size());
        assertEquals(user.getId(), userRepository.findAll().get(0).getId());
        assertEquals(user.getId(), changedUser.getId());
        assertEquals(userWithNameAgeAndGender.getName(), changedUser.getName());
        assertEquals(userWithNameAgeAndGender.getGender(), changedUser.getGender());
        assertEquals(userWithNameAgeAndGender.getAge(), changedUser.getAge());
    }

    @Test
    void addNameAgeAndGenderThrowsBadRequestExceptionForNotValidUser() {
        // given
        User userWithNullName = getNotValidIncompleteUser(UserField.NAME);
        User userWithNullGender = getNotValidIncompleteUser(UserField.GENDER);
        User userWithNullAge = getNotValidIncompleteUser(UserField.AGE);

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(null);
        });
        // then
        assertEquals(env.getProperty("error.UserNameAgeOrGenderCantBeNull"), exception.getMessage());

        // when
        Throwable nameException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullName);
        });
        // then
        assertEquals(env.getProperty("error.UserNameAgeOrGenderCantBeNull"), nameException.getMessage());

        // when
        Throwable ageException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullAge);
        });
        // then
        assertEquals(env.getProperty("error.UserNameAgeOrGenderCantBeNull"), ageException.getMessage());

        // when
        Throwable genderException = assertThrows(BadRequestException.class, () -> {
            userService.addNameAgeAndGender(userWithNullGender);
        });
        // then
        assertEquals(env.getProperty("error.UserNameAgeOrGenderCantBeNull"), genderException.getMessage());
    }


    // =========== addNewUsersForMatching(User user, List<User> usersForMatching, Update.Position position) ============
    @Test
    void addNewUsersForMatching() {
        // given
        User user = populateDb.populateDbWithAnonimUser("name");
        User userForMatching1 = populateDb.populateDbWithAnonimUser("userForMatching1");
        User userForMatching2 = populateDb.populateDbWithAnonimUser("userForMatching2");
        User userForMatching3 = populateDb.populateDbWithAnonimUser("userForMatching3");
        List<User> usersForMatching = Arrays.asList(userForMatching1, userForMatching2, userForMatching3);

        // when
        User updatedUser = userService.addNewUsersForMatching(user, usersForMatching, Update.Position.LAST);

        // then
        assertEquals(3, updatedUser.getUsersForMatchingId().size());
        assertTrue(usersForMatching
                        .stream()
                        .allMatch(userForMatching -> updatedUser.getUsersForMatchingId().contains(userForMatching.getId())));
    }

    @Test
    void addNewUsersForMatchingThrowsNullPointerExceptionForNullUsersForMatching() {
        // given
        User user = populateDb.populateDbWithAnonimUser("name");

        // when
        assertThrows(NullPointerException.class, () ->
                userService.addNewUsersForMatching(user, null, Update.Position.LAST));
    }

    @Test
    void addNewUsersForMatchingThrowsExceptionForNotExistingUser() {
        // given
        User user = new User();
        user.setId(new ObjectId());
        User userForMatching1 = populateDb.populateDbWithAnonimUser("userForMatching1");
        User userForMatching2 = populateDb.populateDbWithAnonimUser("userForMatching2");
        User userForMatching3 = populateDb.populateDbWithAnonimUser("userForMatching3");
        List<User> usersForMatching = Arrays.asList(userForMatching1, userForMatching2, userForMatching3);

        // when
        User updatedUser = userService.addNewUsersForMatching(user, usersForMatching, Update.Position.LAST);

        // then
        assertEquals(null, updatedUser);
    }


    // ================================================ find(User user) ================================================
    @Test
    void findSuccess() {
        // given
        User user = populateDb.populateDbWithRegisteredUser("name", false);

        // when
        User foundUser = userService.find(user);

        // then
        assertEquals(user, foundUser);
    }

    @Test
    void findThrowsExceptionForNullUser() {
        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.find(null);
        });

        // then
        assertEquals(env.getProperty("error.UserOrUserIdCan`tBeNull"), exception.getMessage());
    }

    @Test
    void findThrowsExceptionForNullUserId() {
        // given
        User user = new User();
        user.setId(null);

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.find(user);
        });

        // then
        assertEquals(env.getProperty("error.UserOrUserIdCan`tBeNull"), exception.getMessage());
    }

    @Test
    void findThrowsExceptionForNotExistingUserId() {
        // given
        User notPopulatedToDbUser = new User();
        notPopulatedToDbUser.setId(new ObjectId());

        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.find(notPopulatedToDbUser);
        });

        // then
        assertTrue(exception.getMessage().contains(env.getProperty("error.noUserFound")));
    }


    // ===================================== findById(ObjectId userId) =================================================

    @Test
    void findByIdSuccess() {
        // given
        User user = populateDb.populateDbWithRegisteredUser("name", false);

        // when
        assertDoesNotThrow(() -> userService.findById(user.getId()));
        User foundUser = userService.findById(user.getId());

        // then
        assertEquals(user, foundUser);
    }

    @Test
    void findByIdThrowsExceptionForNullUserId() {
        // when
        assertThrows(IllegalArgumentException.class, () -> {
            userService.findById(null);
        });
    }

    @Test
    void findByIdThrowsExceptionForNotExistingUserId() {
        // given
        ObjectId notExistingUserId = new ObjectId();

        // when
         Throwable exception = assertThrows(BadRequestException.class, () -> {
             userService.findById(notExistingUserId);
         });

        // then
        assertTrue(exception.getMessage().contains(env.getProperty("error.noUserFound")));
    }


    // ============================= findUserByNameOrEmail(String nameOrEmail) =========================================

    @Test
    void findByNameOrEmailSuccess() {
        // given
        User user = populateDb.populateDbWithRegisteredUser("name", false);

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
    }

    @Test
    void findByNameOrEmailThrowsExceptionForNullNameOrEmail() {
        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> {
            userService.findUserByNameOrEmail(null);
        });

        // then
        assertEquals(env.getProperty("error.UserNameOrEmailCan`tBeNull"), exception.getMessage());
    }

    @Test
    void findByNameOrEmailThrowsExceptionForNotExistingName() {
        // NAME
        // when
        Throwable nameException = assertThrows(BadRequestException.class, () -> {
            userService.findUserByNameOrEmail("notExistingName");
        });
        // then
        assertTrue(nameException.getMessage().contains(env.getProperty("error.noUserFound")));

        // EMAIL
        // when
        Throwable emailException = assertThrows(BadRequestException.class, () -> {
            userService.findUserByNameOrEmail("notExistingEmail@gmail.com");
        });
        // then
        assertTrue(emailException.getMessage().contains(env.getProperty("error.noUserFound")));
    }


    // ========================================= getPrincipalUser() ====================================================
    @Test
    void getPrincipalUserSuccess() {
        // given
        User user = populateDb.populateDbWithAnonimUser("anonimUser");
        TokenPrincipal tokenPrincipal = getTokenPrincipal(user);

        // mocked security: getting principal user is mocked
        given(authService.getAuthPrincipal()).willReturn(tokenPrincipal);

        // when
        User principalUser = userService.getPrincipalUser();

        // then
        assertEquals(tokenPrincipal.getId(), principalUser.getId());
        assertEquals(tokenPrincipal.getUsername(), principalUser.getName());
        assertEquals(tokenPrincipal.getRole(), principalUser.getRole());
    }

    @Test
    void getPrincipalUserThrowsAuthorizationExceptionForNotAuthorisedUser() {
        // given
        given(authService.getAuthPrincipal()).willReturn(null);

        // when
        Throwable exception = assertThrows(AuthorizationException.class, () -> {
            userService.getPrincipalUser();
        });

        // then
        AuthorizationException authorizationException = (AuthorizationException) exception;
        assertEquals(env.getProperty("error.UserNotAuthorised"), exception.getMessage());
        assertEquals(ErrorEnum.NOT_AUTHORIZED, authorizationException.getError());
    }


    // ========================== checkIfUsernameOrEmailExist(String nameOrEmail) ======================================

    @Test
    void checkIfUsernameOrEmailExistThrowsExceptionIfNameExist() {
        // given
        User user = populateDb.populateDbWithRegisteredUser("name", false);

        // when
        Throwable nameException = assertThrows(BadRequestException.class, () ->
                userService.checkIfUsernameOrEmailExist(user.getName()));
        // then
        assertTrue(nameException.getMessage().contains(env.getProperty("error.UserWithTheseNameOrEmailAlreadyExists")));
    }

    @Test
    void checkIfUsernameOrEmailExistThrowsExceptionIfEmailExist() {
        // given
        User user = populateDb.populateDbWithRegisteredUser("name", false);

        // when
        Throwable emailException = assertThrows(BadRequestException.class, () ->
                userService.checkIfUsernameOrEmailExist(user.getEmail()));
        // then
        assertTrue(emailException.getMessage().contains(env.getProperty("error.UserWithTheseNameOrEmailAlreadyExists")));
    }

    @Test
    void checkIfUsernameOrEmailExistDoesNotThrowExceptionIfNameOrEmailDoesNotExist() {
        assertDoesNotThrow(() -> userService.checkIfUsernameOrEmailExist("notExistingNameOrEmail"));
    }

    @Test
    void checkIfUsernameOrEmailExistThrowsExceptionForNullNameOrEmail() {
        // when
        Throwable exception = assertThrows(BadRequestException.class, () -> userService.checkIfUsernameOrEmailExist(null));
        // then
        assertEquals(env.getProperty("error.UserNameOrEmailCan`tBeNull"), exception.getMessage());
    }



    // ================================= ifExistById(ObjectId userId) =============================================
    @Test
    void ifExistByIdReurnTrueIfUserExist() {
        // given
        User user = populateDb.populateDbWithRegisteredUser("name", false);

        // when
        assertTrue(userService.ifExistById(user.getId()));
    }

    @Test
    void ifExistByIdReturnFalseIfUserDoesNotExist() {
        // when
        assertFalse(userService.ifExistById(new ObjectId()));
    }

    @Test
    void ifExistByIdThrowsExceptionForNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.ifExistById(null);
        });
    }


    // ======================================= updateUser(User user) ===================================================







    // ============================================== private ==========================================================
    private enum UserField {
        NAME,
        GENDER,
        AGE;
    }
    private User getNotValidIncompleteUser(UserField notValidUserField) {
        User notValidUser = new User();
        notValidUser.setName(notValidUserField.equals(UserField.NAME) ? null : "name");
        notValidUser.setGender(notValidUserField.equals(UserField.GENDER) ? null : Gender.FEMALE);
        notValidUser.setAge(notValidUserField.equals(UserField.AGE) ? null : 35);
        return notValidUser;
    }

}
