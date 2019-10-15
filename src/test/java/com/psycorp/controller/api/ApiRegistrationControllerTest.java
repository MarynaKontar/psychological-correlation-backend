package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import com.psycorp.model.dto.ChangePasswordDto;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static com.psycorp.FixtureObjectsForTest.fixtureChangePasswordDto;
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
 * For server use {@link MockMvc}.
 * Use not embedded mongo database described in application-test.yml
 */
public class ApiRegistrationControllerTest extends AbstractControllerTest{

    /**
     * Test successful registration (controller + security layers), services, dto and server are mocked.
     * @throws Exception
     */
    @Test
    public void registerSuccess() throws Exception {
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
    public void registerThrowsExceptionForNullCredentialsDto() throws Exception {
        //when
        MvcResult mvcResult  = mockMvc.perform(post("/registration"))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();
        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Required request body is missing"));
    }


    // ========================= changePassword() ==============================
    @Test
    public void successfulChangePassword() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbForChangePasswordTest();
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
    void failedChangePasswordWithFailedToken() throws Exception {
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
    void failedChangePasswordWithNulChangePasswordDto() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbForChangePasswordTest();
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

}