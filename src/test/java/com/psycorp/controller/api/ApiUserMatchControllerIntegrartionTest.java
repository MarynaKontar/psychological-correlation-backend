package com.psycorp.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.ValueProfileMatchingDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.security.TokenEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.psycorp.ObjectsForTests.getValueCompatibilityEntity;
import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiUserMatchControllerIntegrartionTest extends AbstractControllerTest {


    // ================================ /match/getUsersForMatching =====================================================
    // ================= ResponseEntity<List<SimpleUserDto>> getUsersForMatching( ======================================
    // ======= @RequestHeader(value = "userForMatchingToken", required = false) String userForMatchingToken) ===========

    @Test
    void getUsersForMatchingSuccessForNullUserForMatchingToken() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken(true);
        User principal = (User) populatedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntity = populateDbWithValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntity.getUserAnswers(), principal, Area.TOTAL);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/match/getUsersForMatching")
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isOk())
                .andReturn();



        // then
        List<SimpleUserDto> usersForMatching = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                                                                new TypeReference<List<SimpleUserDto>>() {});

        assertEquals(2, usersForMatching.size());

        Optional<User> userForMatching1 = userRepository.findById(usersForMatching.get(0).getId());
        Optional<User> userForMatching2 = userRepository.findById(usersForMatching.get(1).getId());

        assertTrue(userForMatching1.isPresent());
        assertTrue(userForMatching2.isPresent());
        assertEquals(principal.getUsersForMatchingId(), Arrays.asList(userForMatching1.get().getId(), userForMatching2.get().getId()));
        assertEquals(principal.getId(), userForMatching1.get().getUsersForMatchingId().get(0));
        assertEquals(principal.getId(), userForMatching2.get().getUsersForMatchingId().get(0));
    }

    @Test
    void getUsersForMatchingSuccessForUserForMatchingToken() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken(true);
        User principal = (User) populatedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");
        TokenEntity tokenEntityForUserForMatching = tokenRepository.findByUserId(principal.getUsersForMatchingId().get(0)).get();

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntity = populateDbWithValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntity.getUserAnswers(), principal, Area.TOTAL);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/match/getUsersForMatching")
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken())
                .header("userForMatchingToken", ACCESS_TOKEN_PREFIX + " " + tokenEntityForUserForMatching.getToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        // then
        List<SimpleUserDto> usersForMatching = mapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<SimpleUserDto>>() {});

        assertEquals(1, usersForMatching.size());
        Optional<User> userForMatching1 = userRepository.findById(usersForMatching.get(0).getId());
        assertTrue(userForMatching1.isPresent());
    }

    @Test
    void getUsersForMatchingThrowsAuthorizationExceptionForFailedUserForMatchingToken() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken(true);
        User principal = (User) populatedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntity = populateDbWithValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntity.getUserAnswers(), principal, Area.TOTAL);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/match/getUsersForMatching")
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken())
                .header("userForMatchingToken", ACCESS_TOKEN_PREFIX + " " + "FAILED_TOKEN"))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();

        // then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains(ErrorEnum.TOKEN_EXPIRED.getMessage()));
    }


    // ================================ /match/value-profile-for-matching =====================================================
    @Test
    void getValueProfilesForMatching() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken(true);
        User principal = (User) populatedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");
        User userForMatching = userRepository.findById(principal.getUsersForMatchingId().get(0)).get();

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntityForPrincipal = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntityForPrincipal = populateDbWithValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntityForPrincipal.getUserAnswers(), principal, Area.TOTAL);

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntityForUserForMatching = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntityForPrincipal = populateDbWithValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntityForUserForMatching.getUserAnswers(), userForMatching, Area.TOTAL);

        // when
        MvcResult mvcResult = mockMvc.perform(post("/match/value-profile-for-matching")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken())
                .content(mapper.writeValueAsString(userForMatching.getId())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        // then
        ValueProfileMatchingDto responseBody = mapper.readValue(mvcResult.getResponse().getContentAsString(), ValueProfileMatchingDto.class);
        assertEquals(2, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(2, responseBody.getValueProfiles().size());
    }

    @Test
    void getUserMatchPercent() {

    }

    @Test
    void handleException() {
    }
}