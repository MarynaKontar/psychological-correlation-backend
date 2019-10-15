package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psycorp.model.dto.ChangePasswordDto;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.service.CredentialsService;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.CredentialsDtoConverter;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Date;
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
 * For server use {@link MockMvc}.
 * Use not embedded mongo database described in application-test.yml
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiRegistrationControllerIntegrationTest {

    private String oldPassword = "oldPassword";
    private String newPassword = "newPassword";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CredentialsService credentialsService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserAccountDtoConverter userAccountDtoConverter;
    @Autowired
    private CredentialsDtoConverter credentialsDtoConverter;
    @Autowired
    private UserService userService;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(UserAccountEntity.class);
        mongoTemplate.dropCollection(CredentialsEntity.class);
        mongoTemplate.dropCollection(TokenEntity.class);
        mongoTemplate.dropCollection(ValueCompatibilityAnswersEntity.class);
        mongoTemplate.dropCollection(UserMatchEntity.class);
    }


    //  ========================= register(@RequestBody @NotNull @Valid CredentialsDto credentialsDto,=================
    //  ========================= @RequestHeader(value = "Authorization", required = false) String token) =============

    /**
     * Test success registration for new user (there isn't "Authorization" in request header)
     * @throws Exception (JsonProcessingException and UnsupportedEncodingException for mapper and Exception for perform)
     */
    @Test
    void registerSuccessNewUser() throws Exception {
        //given
        fixtureCredentialsDto();
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

        // asserts that user user, user account, credentials and token was created
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
    void registerSuccessExistsUser() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbForSuccessfulRegistrationOfExistsUserTest();
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

    @Test
    public void registerThrowExceptionForNotValidToken() throws Exception {
        //given
        fixtureCredentialsDto();
        CredentialsDto credentialsDto = Fixture.from(CredentialsDto.class).gimme("credentialsRegistrationDto");

        //when
        mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + "notValidToken"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void registerThrowsExceptionForNullCredentialsDto() throws Exception {
        //when
        MvcResult mvcResult  = mockMvc.perform(post("/registration"))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Required request body is missing"));
    }

    @Test
    public void registerThrowsExceptionForNullCredentialsDtoFields() throws Exception {
        //given
        fixtureMissingCredentialsDto();
        CredentialsDto credentialsDtoNullName = Fixture.from(CredentialsDto.class).gimme("credentialsDtoNullName");
        CredentialsDto credentialsDtoNullEmail = Fixture.from(CredentialsDto.class).gimme("credentialsDtoNullEmail");
        CredentialsDto credentialsDtoNullPassword = Fixture.from(CredentialsDto.class).gimme("credentialsDtoNullPassword");
        CredentialsDto credentialsDtoNullGender = Fixture.from(CredentialsDto.class).gimme("credentialsDtoNullGender");
        CredentialsDto credentialsDtoNullAge = Fixture.from(CredentialsDto.class).gimme("credentialsDtoNullAge");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDtoNullName))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(header().exists("messageError"))
                .andExpect(status().is(400))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Validation failed for argument"));

        //when
        mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDtoNullEmail))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(header().exists("messageError"))
                .andExpect(status().is(400))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Validation failed for argument"));

        //when
        mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDtoNullPassword))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(header().exists("messageError"))
                .andExpect(status().is(400))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Validation failed for argument"));

        //when
        mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDtoNullGender))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(header().exists("messageError"))
                .andExpect(status().is(400))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Validation failed for argument"));

        //when
        mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDtoNullAge))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(header().exists("messageError"))
                .andExpect(status().is(400))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Validation failed for argument"));
    }


    // ========================= changePassword() ==============================
    @Test
    void successfulChangePassword() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbForChangePasswordTest();
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
    void failedChangePasswordWithFailedOldPassword() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbForChangePasswordTest();
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

    private Map<String, Object> populateDbForChangePasswordTest() {
        fixtureRegisteredUser();
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

        UserAccountEntity userAccountEntity = userAccountService.insert(user);

        Map<String, Object> preparedObjects = new HashMap<>(4);
        preparedObjects.put("user", user);
        preparedObjects.put("tokenEntity", tokenEntity);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        preparedObjects.put("userAccountEntity", userAccountEntity);
        return preparedObjects;
    }

    private Map<String, Object> populateDbForSuccessfulRegistrationOfExistsUserTest() {
        fixtureCredentialsDto();

        // add to db anonim user and token for it
        User user = userService.createAnonimUser();

        TokenEntity tokenEntity = tokenService.generateAccessTokenForAnonim(user);
        tokenEntity.setType(TokenType.INVITE_TOKEN);
        tokenEntity = tokenRepository.save(tokenEntity);

        Map<String, Object> preparedObjects = new HashMap<>(2);
        preparedObjects.put("user", user);
        preparedObjects.put("tokenEntity", tokenEntity);

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