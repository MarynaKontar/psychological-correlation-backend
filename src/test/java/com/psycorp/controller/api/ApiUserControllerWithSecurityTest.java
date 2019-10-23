package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserDtoConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.psycorp.FixtureObjectsForTest.fixtureIncompleteSimpleUserDto;
import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ApiUserController} and security layer.
 * Service layer, dto and server are mocked.
 * For server uses {@link MockMvc}.
 * Use not embedded mongo database described in application-test.yml
 */
class ApiUserControllerWithSecurityTest extends AbstractControllerTest {

    @MockBean
    private UserService userService;
    @MockBean
    private UserDtoConverter userDtoConverter;


    //  ========================= /user/incompleteRegistration =========================================================
    //  ========================= ResponseEntity<SimpleUserDto> incompleteRegistration( ================================
    //  ========================= @RequestBody @NotNull @Valid SimpleUserDto userDto) ==================================
    @Test
    void incompleteRegistrationSuccess() throws Exception {
        //given
        // add to db anonim user, credentialsEntity and tokenEntity for it
        User anonimUser = populateDbWithAnonimUser();
        CredentialsEntity credentialsEntity = populateDbWithCredentialsEntity(anonimUser, null);
        TokenEntity tokenEntity = populateDbWithTokenEntity(anonimUser, TokenType.ACCESS_TOKEN);

        fixtureIncompleteSimpleUserDto();
        SimpleUserDto requestDto = Fixture.from(SimpleUserDto.class).gimme("incompleteSimpleUserDto");
        requestDto.setId(anonimUser.getId());

        given(userDtoConverter.transform(userService.addNameAgeAndGender(userDtoConverter.transform(requestDto)))).willReturn(requestDto);

        //when
        MvcResult mvcResult =
                mockMvc.perform(
                        post("/user/incompleteRegistration")
                                .content(mapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn();

        //then
        SimpleUserDto responseDto = mapper.readValue(mvcResult.getResponse().getContentAsString(), SimpleUserDto.class);
        assertEquals(requestDto, responseDto);
    }

    @Test
    void incompleteRegistrationThrowsExceptionForFailedToken() throws Exception {
        //given
        fixtureIncompleteSimpleUserDto();
        SimpleUserDto requestDto = Fixture.from(SimpleUserDto.class).gimme("incompleteSimpleUserDto");

        //when
        mockMvc.perform(post("/user/incompleteRegistration")
                .content(mapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + "FAILED_TOKEN"))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    void incompleteRegistrationThrowsExceptionForNullSimpleUserDto() throws Exception {
        //given
        // add to db anonim user, credentialsEntity and tokenEntity for it
        User anonimUser = populateDbWithAnonimUser();
        CredentialsEntity credentialsEntity = populateDbWithCredentialsEntity(anonimUser, null);
        TokenEntity tokenEntity = populateDbWithTokenEntity(anonimUser, TokenType.ACCESS_TOKEN);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/user/incompleteRegistration")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Required request body is missing"));
    }

}
