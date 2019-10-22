package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.security.TokenEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static com.psycorp.FixtureObjectsForTest.fixtureMissingIncompleteSimpleUserDto;
import static com.psycorp.FixtureObjectsForTest.fixtureSimpleUserDto;
import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ApiUserController}.
 * For server uses {@link MockMvc}.
 * Use not embedded mongo database described in application-test.yml
 */
class ApiUserControllerIntegrationTest extends AbstractControllerTest {


    //  ========================= /user/incompleteRegistration =========================================================
    //  ========================= ResponseEntity<SimpleUserDto> incompleteRegistration( ================================
    //  ========================= @RequestBody @NotNull @Valid SimpleUserDto userDto) ==================================

    @Test
    void incompleteRegistrationSuccess() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        User anonimUser = (User) preparedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) preparedObjects.get("tokenEntity");

        fixtureSimpleUserDto();
        SimpleUserDto requestDto = Fixture.from(SimpleUserDto.class).gimme("simpleUserDto");
        requestDto.setId(anonimUser.getId());

        //when
        MvcResult mvcResult = mockMvc.perform(post("/user/incompleteRegistration")
                .content(mapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        //then
        SimpleUserDto responseDto = mapper.readValue(mvcResult.getResponse().getContentAsString(), SimpleUserDto.class);
        requestDto.setEmail(null);
        assertEquals(requestDto, responseDto);
        assertEquals(responseDto.getEmail(), null);
    }

    @Test
    void incompleteRegistrationThrowsExceptionForNullSimpleUserDtoFields() throws Exception {
        //given
        Map<String, Object> preparedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) preparedObjects.get("tokenEntity");

        fixtureMissingIncompleteSimpleUserDto();
        SimpleUserDto simpleUserDtoNullName = Fixture.from(SimpleUserDto.class).gimme("incompleteSimpleUserDtoNullName");
        SimpleUserDto simpleUserDtoNullGender = Fixture.from(SimpleUserDto.class).gimme("incompleteSimpleUserDtoNullGender");
        SimpleUserDto simpleUserDtoNullAge = Fixture.from(SimpleUserDto.class).gimme("incompleteSimpleUserDtoNullAge");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/user/incompleteRegistration")
                .content(mapper.writeValueAsString(simpleUserDtoNullName))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().is(400))
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains(env.getProperty("error.UserNameAgeOrGenderCantBeNull")));

        //when
        mvcResult = mockMvc.perform(post("/user/incompleteRegistration")
                .content(mapper.writeValueAsString(simpleUserDtoNullGender))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().is(400))
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains(env.getProperty("error.UserNameAgeOrGenderCantBeNull")));

        //when
        mvcResult = mockMvc.perform(post("/user/incompleteRegistration")
                .content(mapper.writeValueAsString(simpleUserDtoNullAge))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().is(400))
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains(env.getProperty("error.UserNameAgeOrGenderCantBeNull")));
    }

}
