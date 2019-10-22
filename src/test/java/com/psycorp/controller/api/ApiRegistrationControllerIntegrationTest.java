package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import com.psycorp.model.dto.ChangePasswordDto;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.psycorp.FixtureObjectsForTest.*;
import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ApiRegistrationController}.
 * For server uses {@link MockMvc}.
 * Use not embedded mongo database described in application-test.yml
 */
class ApiRegistrationControllerIntegrationTest extends AbstractControllerTest{

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserAccountService userAccountService;


    //  ========= /registration ========================================================================================
    //  ========= ResponseEntity<UserAccountDto> register(@RequestBody @NotNull @Valid CredentialsDto credentialsDto,===
    //  ========= @RequestHeader(value = "Authorization", required = false) String token) ==============================

    /**
     * Test success registration for new user (there isn't "Authorization" in request header)
     * @throws Exception (JsonProcessingException and UnsupportedEncodingException for mapper and Exception for perform)
     */
    @Test
    void registerSuccessForNewUser() throws Exception {
        //given
        CredentialsDto credentialsDto = Fixture.from(CredentialsDto.class).gimme("credentialsRegistrationDto");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        //then
        // map response from string to ValueCompatibilityAnswersDto
        UserAccountDto userAccountDto = mapper
                .readValue(mvcResult.getResponse().getContentAsString(), UserAccountDto.class);

        // asserts that user, user account, credentials and token was created
        Optional<CredentialsEntity> credentialsEntityOptional = credentialsRepository.findByUserId(userAccountDto.getUser().getId());
        assertDoesNotThrow(() -> userService.findUserByNameOrEmail(credentialsDto.getName()));
        assertNotNull(userAccountService.getUserAccountEntityByUserIdOrNull(userAccountDto.getUser().getId()));
        assertTrue(credentialsEntityOptional.isPresent());
        assertDoesNotThrow(() -> tokenService.findByUserId(userAccountDto.getUser().getId()));
        assertEquals(userRepository.findAll().size(), 1);
        assertEquals(userAccountRepository.findAll().size(), 1);
        assertEquals(credentialsRepository.findAll().size(), 1);
        assertEquals(tokenRepository.findAll().size(), 1);

        // asserts that user information from credentialsDto is saved id db
        User user = userService.findUserByNameOrEmail(credentialsDto.getName());
        assertUserWithCredentialsDto(user, credentialsDto);

        // asserts that saved user information from credentialsDto is correctly transmitted to userAccountDto
        assertUserAccountDtoWithCredentialsDto(userAccountDto, credentialsDto);
        assertTrue(userAccountDto.getUser().getId().equals(user.getId()));

        // asserts that password was saved correctly
        assertTrue(passwordEncoder.matches(credentialsDto.getPassword(), credentialsEntityOptional.get().getPassword()));

        // asserts that tokenEntity has ACCESS_TOKEN and token was injected to response header
        TokenEntity tokenEntity = tokenService.findByUserId(userAccountDto.getUser().getId());
        assertEquals(tokenEntity.getType(), TokenType.ACCESS_TOKEN);
        assertEquals(mvcResult.getResponse().getHeader("AUTHORIZATION"),
                ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken());
    }


    @Test
    void registerSuccessForExistsUser() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        User principal = (User) preparedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) preparedObjects.get("tokenEntity");
        CredentialsDto credentialsDto = Fixture.from(CredentialsDto.class).gimme("credentialsRegistrationDto");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        //then
        // map response from string to UserAccountDto
        UserAccountDto userAccountDto = mapper
                .readValue(mvcResult.getResponse().getContentAsString(), UserAccountDto.class);

        // asserts that user user, user account, credentials and token are in db
        Optional<CredentialsEntity> credentialsEntityOptional = credentialsRepository.findByUserId(principal.getId());
        assertDoesNotThrow(() -> userService.findUserByNameOrEmail(credentialsDto.getName()));
        assertNotNull(userAccountService.getUserAccountEntityByUserIdOrNull(principal.getId()));
        assertTrue(credentialsEntityOptional.isPresent());
        assertDoesNotThrow(() -> tokenService.findByUserId(principal.getId()));
        assertEquals(userRepository.findAll().size(), 1);
        assertEquals(userAccountRepository.findAll().size(), 1);
        assertEquals(credentialsRepository.findAll().size(), 1);
        assertEquals(tokenRepository.findAll().size(), 1);

        // asserts that user information from credentialsDto is saved id db
        User updatedUser = userService.findById(principal.getId());
        assertUserWithCredentialsDto(updatedUser, credentialsDto);

        // asserts that saved user information from credentialsDto is correctly transmitted to userAccountDto
        assertUserAccountDtoWithCredentialsDto(userAccountDto, credentialsDto);
        assertTrue(userAccountDto.getUser().getId().equals(updatedUser.getId()));

        // asserts that password was saved correctly
        assertTrue(passwordEncoder.matches(credentialsDto.getPassword(), credentialsEntityOptional.get().getPassword()));

        // asserts that tokenEntity has ACCESS_TOKEN and token was injected to response header
        TokenEntity updatedTokenEntity = tokenService.findByUserId(principal.getId());
        assertEquals(updatedTokenEntity.getType(), TokenType.ACCESS_TOKEN);
        assertEquals(mvcResult.getResponse().getHeader("AUTHORIZATION"),
                ACCESS_TOKEN_PREFIX + " " + updatedTokenEntity.getToken());
    }


    // ========================= /registration/changePassword ==========================================================
    // ========================= changePassword(@RequestBody @NotNull ChangePasswordDto changePasswordDto) =============
    @Test
    void changePasswordSuccessForValidTokenAndOldPassword() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbForChangePasswordIntegrationTest();
        User user = (User) preparedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) preparedObjects.get("tokenEntity");

        fixtureChangePasswordDto(oldPassword, newPassword);
        ChangePasswordDto changePasswordDto = Fixture.from(ChangePasswordDto.class).gimme("changePasswordDto");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/registration/changePassword")
                .content(mapper.writeValueAsString(changePasswordDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isNoContent())
                .andExpect(header().doesNotExist("AUTHORIZATION"))
                .andReturn();

        //then
        assertTrue(credentialsRepository.findByUserId(user.getId()).isPresent());
        String newSavedPassword = credentialsRepository.findByUserId(user.getId()).get().getPassword();
        assertTrue(passwordEncoder.matches(newPassword, newSavedPassword));

        assertEquals(userRepository.findAll().size(), 1);
        assertEquals(userAccountRepository.findAll().size(), 1);
        assertEquals(credentialsRepository.findAll().size(), 1);
        assertEquals(tokenRepository.findAll().size(), 1);
    }

    @Test
    void changePasswordFailedForNotValidOldPassword() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbForChangePasswordIntegrationTest();
        TokenEntity tokenEntity = (TokenEntity) preparedObjects.get("tokenEntity");

        fixtureChangePasswordDto("notValidPassword", newPassword);
        ChangePasswordDto changePasswordDto = Fixture.from(ChangePasswordDto.class).gimme("changePasswordDto");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/registration/changePassword")
                .content(mapper.writeValueAsString(changePasswordDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse()
                .getHeader("messageError")
                .contains(env.getProperty("error.YouEnterWrongPassword")));
    }


    //==================== private ==========================================
    private Map<String, Object> populateDbForChangePasswordIntegrationTest() {
        // populate db with user, tokenEntity, credentialsEntity and userAccountEntity
        User user = populateDbWithRegisteredUser();
        TokenEntity tokenEntity = populateDbWithTokenEntity(user, TokenType.ACCESS_TOKEN);
        CredentialsEntity credentialsEntity = populateDbWithCredentialsEntity(user, oldPassword);
        UserAccountEntity userAccountEntity = populateDbWithUserAccountEntity(user);

        Map<String, Object> preparedObjects = new HashMap<>(4);
        preparedObjects.put("user", user);
        preparedObjects.put("tokenEntity", tokenEntity);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        preparedObjects.put("userAccountEntity", userAccountEntity);
        return preparedObjects;
    }

    // ========================= Assertion ==========================
    private void assertUserWithCredentialsDto(User user, CredentialsDto credentialsDto) {
        assertTrue(user.getName().equals(credentialsDto.getName()));
        assertTrue(user.getEmail().equals(credentialsDto.getEmail()));
        assertTrue(user.getGender().equals(credentialsDto.getGender()));
        assertTrue(user.getAge().equals(credentialsDto.getAge()));
        assertTrue(user.getRole().equals(UserRole.USER));
    }

    private void assertUserAccountDtoWithCredentialsDto(UserAccountDto userAccountDto, CredentialsDto credentialsDto) {
        assertTrue(userAccountDto.getUser().getName().equals(credentialsDto.getName()));
        assertTrue(userAccountDto.getUser().getEmail().equals(credentialsDto.getEmail()));
        assertTrue(userAccountDto.getUser().getGender().equals(credentialsDto.getGender()));
        assertTrue(userAccountDto.getUser().getAge().equals(credentialsDto.getAge()));
        assertEquals(userAccountDto.getAccountType(), AccountType.OPEN);
    }

}