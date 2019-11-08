package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private CredentialsRepository credentialsRepository;
    @Mock
    private UserMatchRepository userMatchRepository;
    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
//        prepareUsersForTests();

    }

//    @Test
//    void createAnonimUser() {
//    }

    @Test
    void addNameAgeAndGender() {
//        assertThrows(BadRequestException.class, () -> {
//            userService.addNameAgeAndGender(null);
//        });
    }

    @Test
    void addNewUsersForMatching() {
    }

    @Test
    void find() {
    }

    @Test
    void findById() {
    }

    @Test
    void findUserByNameOrEmail() {
    }

    @Test
    void getPrincipalUser() {
    }

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