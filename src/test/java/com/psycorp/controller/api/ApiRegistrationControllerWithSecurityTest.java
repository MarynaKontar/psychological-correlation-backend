package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import com.psycorp.model.dto.ChangePasswordDto;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.service.implementation.CredentialsServiceImpl;
import com.psycorp.service.security.implementation.TokenServiceImpl;
import com.psycorp.сonverter.CredentialsDtoConverter;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static com.psycorp.FixtureObjectsForTest.fixtureChangePasswordDto;
import static com.psycorp.FixtureObjectsForTest.fixtureMissingCredentialsDto;
import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ApiRegistrationController} and security layer.
 * Service layer, dto and server are mocked.
 * For server uses {@link MockMvc}.
 * Use not embedded mongo database described in application-test.yml
 */
class ApiRegistrationControllerWithSecurityTest extends AbstractControllerTest{
    private final static String oldPassword = "oldPassword";
    private final static String newPassword = "newPassword";

    @MockBean
    CredentialsServiceImpl credentialsService;
    @MockBean
    TokenServiceImpl tokenService;
    @MockBean
    CredentialsDtoConverter credentialsDtoConverter;
    @MockBean
    UserAccountDtoConverter userAccountDtoConverter;

    //  ========================= /registration ========================================================================
    //  ========================= ResponseEntity<UserAccountDto> register(@RequestBody @NotNull @Valid =================
    //  ===== CredentialsDto credentialsDto, @RequestHeader(value = "Authorization", required = false) String token) ===

    /**
     * Test successful registration (controller + security layers), services, dto and server are mocked.
     * @throws Exception
     */
    @Test
    void registerSuccess() throws Exception {
        //given
        Map<String, Object> preparedObjects = prepareObjectsForSuccessfulRegistrationTest();

        TokenEntity tokenEntity = (TokenEntity) preparedObjects.get("tokenEntity");
        CredentialsDto credentialsDto = (CredentialsDto) preparedObjects.get("credentialsDto");
        UserAccount userAccount = (UserAccount) preparedObjects.get("userAccount");
        UserAccountDto userAccountDto = (UserAccountDto) preparedObjects.get("userAccountDto");
        String token = tokenEntity.getToken();

        given(credentialsService.save(credentialsDtoConverter.transform(credentialsDto))).willReturn(userAccount);
        given(tokenService.getTokenForRegisteredUser(ACCESS_TOKEN_PREFIX + " " + token, userAccount.getUser().getId())).willReturn(token);
        given(userAccountDtoConverter.transform(userAccount)).willReturn(userAccountDto);

        //when
        MvcResult mvcResult =
                mockMvc.perform(
                post("/registration")
                        .content(mapper.writeValueAsString(credentialsDto))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + token))
                        .andExpect(status().isCreated())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andExpect(header().exists("AUTHORIZATION"))
                        .andReturn();

        UserAccountDto responseDto = mapper.readValue(mvcResult.getResponse().getContentAsString(), UserAccountDto.class);

        //then
        assertEquals(responseDto, userAccountDto);
    }

    @Test
    void registerIsUnauthorizedForFailedToken() throws Exception {
        //given
        CredentialsDto credentialsDto = Fixture.from(CredentialsDto.class).gimme("credentialsRegistrationDto");

        //when
        mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + "FAILED_TOKEN"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    void registerThrowsExceptionForNullCredentialsDto() throws Exception {
        //when
        MvcResult mvcResult  = mockMvc.perform(post("/registration"))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Required request body is missing"));
    }

    @Test
    void registerThrowsExceptionForNullCredentialsDtoFields() throws Exception {
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
                .andExpect(status().is(400))
                .andExpect(header().exists("messageError"))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Validation failed for argument"));

        //when
        mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDtoNullGender))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is(400))
                .andExpect(header().exists("messageError"))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Validation failed for argument"));

        //when
        mvcResult = mockMvc.perform(post("/registration")
                .content(mapper.writeValueAsString(credentialsDtoNullAge))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is(400))
                .andExpect(header().exists("messageError"))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Validation failed for argument"));
    }

    // ========================= /registration/changePassword ==========================================================
    // ========================= changePassword(@RequestBody @NotNull ChangePasswordDto changePasswordDto) =============
    @Test
    void changePasswordSuccessForValidTokenAndOldPassword() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDb.populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken(false);
        TokenEntity tokenEntity = (TokenEntity) preparedObjects.get("tokenEntity");

        fixtureChangePasswordDto(oldPassword, newPassword);
        ChangePasswordDto changePasswordDto = Fixture.from(ChangePasswordDto.class).gimme("changePasswordDto");
        willDoNothing().given(credentialsService).changePassword(changePasswordDto.getOldPassword(), changePasswordDto.getNewPassword());

        //when
        mockMvc.perform(post("/registration/changePassword")
                .content(mapper.writeValueAsString(changePasswordDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isNoContent())
                .andExpect(header().doesNotExist("AUTHORIZATION"));
    }

    @Test
    void changePasswordIsUnauthorizedForFailedToken() throws Exception {
        //given
        fixtureChangePasswordDto(oldPassword, newPassword);
        ChangePasswordDto changePasswordDto = Fixture.from(ChangePasswordDto.class).gimme("changePasswordDto");

        //when
        mockMvc.perform(post("/registration/changePassword")
                .content(mapper.writeValueAsString(changePasswordDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + "FAILED_TOKEN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordThrowsExceptionForNullChangePasswordDto() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDb.populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken(false);
        TokenEntity tokenEntity = (TokenEntity) preparedObjects.get("tokenEntity");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/registration/changePassword")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Required request body is missing"));
    }


    // ============================================== private ==========================================================
    private Map<String, Object> prepareObjectsForSuccessfulRegistrationTest() {
        // populate db with user, tokenEntity and credentialsEntity
        Map<String, Object> preparedObjects = populateDb.populateDbWithAnonimUserAndCredentialsAndToken();
        User user = (User) preparedObjects.get("user");

        // prepare objects for successful registration test
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

        preparedObjects.put("credentialsDto", credentialsDto);
        preparedObjects.put("userAccount", userAccount);
        preparedObjects.put("userAccountDto", userAccountDto);
        return preparedObjects;
    }

}