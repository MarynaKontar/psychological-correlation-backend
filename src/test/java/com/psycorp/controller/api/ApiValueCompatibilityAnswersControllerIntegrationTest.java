package com.psycorp.controller.api;

import com.psycorp.model.dto.ValueCompatibilityAnswersDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.сonverter.ValueCompatibilityAnswersDtoConverter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.psycorp.ObjectsForTests.getValueCompatibilityAnswersDto;
import static com.psycorp.ObjectsForTests.getValueCompatibilityEntity;
import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ApiValueCompatibilityAnswersController}.
 * For server uses {@link MockMvc}.
 * Use not embedded mongo database described in application-test.yml
 */
class ApiValueCompatibilityAnswersControllerIntegrationTest extends AbstractControllerTest {

    @Autowired
    private ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    @Autowired
    private ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter;


    // ================== /test/initTest ====================================================================================
    // ================== ResponseEntity<ValueCompatibilityAnswersDto> getInitValueCompatibilityAnswers() ==============
    @Test
    void getInitValueCompatibilityAnswersSuccess() throws Exception {

        //when
        MvcResult mvcResult = mockMvc.perform(get("/test/initTest"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        //then
        // map response from string to ValueCompatibilityAnswersDto
        ValueCompatibilityAnswersDto valueCompatibilityAnswersDto = mapper
                .readValue(mvcResult.getResponse().getContentAsString(), ValueCompatibilityAnswersDto.class);

        // GOAL
        assertTrue(valueCompatibilityAnswersDto.getGoal().size() == TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA);
        // chosenScale
        assertTrue(valueCompatibilityAnswersDto.getGoal().stream().allMatch(goal -> goal.getChosenScale().getScale() == null));
        assertTrue(valueCompatibilityAnswersDto.getGoal().stream().allMatch(goal -> goal.getChosenScale().getScaleHeader() == null));
        assertTrue(valueCompatibilityAnswersDto.getGoal().stream().allMatch(goal -> goal.getChosenScale().getScaleDescription() == null));
        // firstScale and secondScale
        for (int value = 0; value < Scale.values().length; value++) {
            Scale finalValue = Scale.values()[value];
            assertTrue(valueCompatibilityAnswersDto.getGoal()
                    .stream()
                    .filter(goal -> goal.getFirstScale().getScale() == finalValue
                                 || goal.getSecondScale().getScale() == finalValue)
                    .count() == 5);
        }

        // QUALITY
        assertTrue(valueCompatibilityAnswersDto.getQuality().size() == TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA);
        // chosenScale
        assertTrue(valueCompatibilityAnswersDto.getQuality().stream().allMatch(quality -> quality.getChosenScale().getScale() == null));
        assertTrue(valueCompatibilityAnswersDto.getQuality().stream().allMatch(quality -> quality.getChosenScale().getScaleHeader() == null));
        assertTrue(valueCompatibilityAnswersDto.getQuality().stream().allMatch(quality -> quality.getChosenScale().getScaleDescription() == null));
        // firstScale and secondScale
        for (int value = 0; value < Scale.values().length; value++) {
            Scale finalValue = Scale.values()[value];
            assertTrue(valueCompatibilityAnswersDto.getQuality()
                    .stream()
                    .filter(quality -> quality.getFirstScale().getScale() == finalValue
                            || quality.getSecondScale().getScale() == finalValue)
                    .count() == 5);
        }

        // STATE
        assertTrue(valueCompatibilityAnswersDto.getState().size() == TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA);
        // chosenScale
        assertTrue(valueCompatibilityAnswersDto.getState().stream().allMatch(state -> state.getChosenScale().getScale() == null));
        assertTrue(valueCompatibilityAnswersDto.getState().stream().allMatch(state -> state.getChosenScale().getScaleHeader() == null));
        assertTrue(valueCompatibilityAnswersDto.getState().stream().allMatch(state -> state.getChosenScale().getScaleDescription() == null));
        // firstScale and secondScale
        for (int value = 0; value < Scale.values().length; value++) {
            Scale finalValue = Scale.values()[value];
            assertTrue(valueCompatibilityAnswersDto.getState()
                    .stream()
                    .filter(state -> state.getFirstScale().getScale() == finalValue
                            || state.getSecondScale().getScale() == finalValue)
                    .count() == 5);
        }

        assertNull(valueCompatibilityAnswersDto.getId());
        assertNull(valueCompatibilityAnswersDto.getUserId());
        assertNull(valueCompatibilityAnswersDto.getPassed());
        assertNull(valueCompatibilityAnswersDto.getPassDate());
    }


    // ================== /test/goal ====================================================================================
    // ================== ResponseEntity<ValueCompatibilityAnswersDto> saveGoal(
    //            @RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto,
    //            @RequestHeader(value = "Authorization", required = false) String token,
    //            @RequestHeader(value = "userForMatchingToken", required = false) String userForMatchingToken)

    @Test
    void saveGoalSuccessForNullToken() throws Exception {
        //given
        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/goal")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("AUTHORIZATION"))
                .andReturn();

        //then
        ValueCompatibilityAnswersDto responseBody = mapper.readValue(mvcResult
                .getResponse()
                .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getGoal().size());
        assertEquals(0, responseBody.getQuality().size());
        assertEquals(0, responseBody.getState().size());

        assertEquals(requestBody.getGoal(), responseBody.getGoal());
        for (int i = 0; i < responseBody.getGoal().size(); i++) {
            assertEquals(requestBody.getGoal().get(i).getChosenScale().getScale(),
                    responseBody.getGoal().get(i).getChosenScale().getScale());
        }

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(0, userAccountRepository.findAll().size());
    }

    @Test
    void saveGoalSuccessForAnonimUser() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/goal")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("AUTHORIZATION"))
                .andReturn();

        //then
        ValueCompatibilityAnswersDto responseBody = mapper.readValue(mvcResult
                .getResponse()
                .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getGoal().size());
        assertEquals(0, responseBody.getQuality().size());
        assertEquals(0, responseBody.getState().size());

        assertEquals(requestBody.getGoal(), responseBody.getGoal());
        for (int i = 0; i < responseBody.getGoal().size(); i++) {
            assertEquals(requestBody.getGoal().get(i).getChosenScale().getScale(),
                    responseBody.getGoal().get(i).getChosenScale().getScale());
        }

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(0, userAccountRepository.findAll().size());
    }

    @Test
    void saveGoalSuccessForRegisteredUser() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/goal")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("AUTHORIZATION"))
                .andReturn();

        //then
        ValueCompatibilityAnswersDto responseBody = mapper.readValue(mvcResult
                .getResponse()
                .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getGoal().size());
        assertEquals(0, responseBody.getQuality().size());
        assertEquals(0, responseBody.getState().size());

        assertEquals(requestBody.getGoal(), responseBody.getGoal());
        for (int i = 0; i < responseBody.getGoal().size(); i++) {
            assertEquals(requestBody.getGoal().get(i).getChosenScale().getScale(),
                    responseBody.getGoal().get(i).getChosenScale().getScale());
        }

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(1, userAccountRepository.findAll().size());
    }

    @Test
    void saveGoalIsUnauthorizedForFailedToken() throws Exception {
        //given
        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/goal")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + "FAILED_TOKEN"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        //then
        assertEquals(0, valueCompatibilityAnswersRepository.findAll().size());
    }

    @Test
    void saveGoalThrowsExceptionForNullValueCompatibilityAnswersDto() throws Exception {
        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/goal")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Required request body is missing"));
    }

    @Test
    void saveGoalThrowsExceptionForNullValueCompatibilityAnswersDtoFields() throws Exception {
        //given
        ValueCompatibilityAnswersDto valueCompatibilityAnswersDto = new ValueCompatibilityAnswersDto();
        valueCompatibilityAnswersDto.setGoal(null);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/goal")
                .content(mapper.writeValueAsString(valueCompatibilityAnswersDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(header().exists("messageError"))
                .andExpect(status().is(400))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains(env.getProperty("error.ItMustBe15TestsForArea")));
    }

    // ================== /test/quality ================================================================================
    // ================== ResponseEntity<ValueCompatibilityAnswersDto> saveQuality(=====================================
    // ============@RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto)==============

    @Test
    void saveQualitySuccessForAnonimUser() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");
        User user = (User) populatedObjects.get("user");

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntity = populateDbWithPartialValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntity.getUserAnswers(), user, Area.GOAL);
        assertDbBeforeSaveQuality(valueCompatibilityAnswersEntity);

        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/quality")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        //then
        ValueCompatibilityAnswersDto responseBody = mapper.readValue(mvcResult
                .getResponse()
                .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getGoal().size());
        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getQuality().size());
        assertEquals(0, responseBody.getState().size());

        assertEquals(requestBody.getGoal(), responseBody.getGoal());
        for (int i = 0; i < responseBody.getGoal().size(); i++) {
            assertEquals(requestBody.getGoal().get(i).getChosenScale().getScale(),
                    responseBody.getGoal().get(i).getChosenScale().getScale());
        }
        assertEquals(requestBody.getQuality(), responseBody.getQuality());
        for (int i = 0; i < responseBody.getQuality().size(); i++) {
            assertEquals(requestBody.getQuality().get(i).getChosenScale().getScale(),
                    responseBody.getQuality().get(i).getChosenScale().getScale());
        }

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(0, userAccountRepository.findAll().size());
    }

    @Test
    void saveQualitySuccessForRegisteredUser() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken();
        User user = (User) populatedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntity = populateDbWithPartialValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntity.getUserAnswers(), user, Area.GOAL);
        assertDbBeforeSaveQuality(valueCompatibilityAnswersEntity);

        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/quality")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        //then
        ValueCompatibilityAnswersDto responseBody = mapper.readValue(mvcResult
                .getResponse()
                .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getGoal().size());
        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getQuality().size());
        assertEquals(0, responseBody.getState().size());

        assertEquals(requestBody.getGoal(), responseBody.getGoal());
        for (int i = 0; i < responseBody.getGoal().size(); i++) {
            assertEquals(requestBody.getGoal().get(i).getChosenScale().getScale(),
                    responseBody.getGoal().get(i).getChosenScale().getScale());
        }
        assertEquals(requestBody.getQuality(), responseBody.getQuality());
        for (int i = 0; i < responseBody.getQuality().size(); i++) {
            assertEquals(requestBody.getQuality().get(i).getChosenScale().getScale(),
                    responseBody.getQuality().get(i).getChosenScale().getScale());
        }

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(1, userAccountRepository.findAll().size());
    }

    @Test
    void saveQualityIsUnauthorizedForFailedToken() throws Exception {
        //given
        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/quality")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + "FAILED_TOKEN"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        //then
        assertEquals(0, valueCompatibilityAnswersRepository.findAll().size());
    }

    @Test
    void saveQualityThrowsExceptionForNullValueCompatibilityAnswersDto() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/quality")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Required request body is missing"));
    }

    @Test
    void saveQualityThrowsExceptionForNullValueCompatibilityAnswersDtoFields() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        ValueCompatibilityAnswersDto valueCompatibilityAnswersDto = new ValueCompatibilityAnswersDto();
        valueCompatibilityAnswersDto.setQuality(null);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/quality")
                .content(mapper.writeValueAsString(valueCompatibilityAnswersDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(header().exists("messageError"))
                .andExpect(status().is(400))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains(env.getProperty("error.ItMustBe15TestsForArea")));
    }


// ================== /test/state ================================================================================
    // ================== ResponseEntity<ValueCompatibilityAnswersDto> saveQuality(=====================================
    // ============@RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto)==============

    @Test
    void saveStateSuccessForAnonimUser() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");
        User user = (User) populatedObjects.get("user");

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntity = populateDbWithPartialValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntity.getUserAnswers(), user, Area.QUALITY);
        assertDbBeforeSaveState(valueCompatibilityAnswersEntity);

        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/state")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        //then
        ValueCompatibilityAnswersDto responseBody = mapper.readValue(mvcResult
                .getResponse()
                .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getGoal().size());
        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getQuality().size());
        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getState().size());

        assertEquals(requestBody.getGoal(), responseBody.getGoal());
        for (int i = 0; i < responseBody.getGoal().size(); i++) {
            assertEquals(requestBody.getGoal().get(i).getChosenScale().getScale(),
                    responseBody.getGoal().get(i).getChosenScale().getScale());
        }
        assertEquals(requestBody.getQuality(), responseBody.getQuality());
        for (int i = 0; i < responseBody.getQuality().size(); i++) {
            assertEquals(requestBody.getQuality().get(i).getChosenScale().getScale(),
                    responseBody.getQuality().get(i).getChosenScale().getScale());
        }
        assertEquals(requestBody.getState(), responseBody.getState());
        for (int i = 0; i < responseBody.getState().size(); i++) {
            assertEquals(requestBody.getState().get(i).getChosenScale().getScale(),
                    responseBody.getState().get(i).getChosenScale().getScale());
        }

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(0, userAccountRepository.findAll().size());
    }

    @Test
    void saveStateSuccessForRegisteredUser() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken();
        User user = (User) populatedObjects.get("user");
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = getValueCompatibilityEntity();
        valueCompatibilityAnswersEntity = populateDbWithPartialValueCompatibilityAnswersEntity(
                valueCompatibilityAnswersEntity.getUserAnswers(), user, Area.QUALITY);
        assertDbBeforeSaveState(valueCompatibilityAnswersEntity);

        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/state")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        //then
        ValueCompatibilityAnswersDto responseBody = mapper.readValue(mvcResult
                .getResponse()
                .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getGoal().size());
        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getQuality().size());
        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA, responseBody.getState().size());

        assertEquals(requestBody.getGoal(), responseBody.getGoal());
        for (int i = 0; i < responseBody.getGoal().size(); i++) {
            assertEquals(requestBody.getGoal().get(i).getChosenScale().getScale(),
                    responseBody.getGoal().get(i).getChosenScale().getScale());
        }
        assertEquals(requestBody.getQuality(), responseBody.getQuality());
        for (int i = 0; i < responseBody.getQuality().size(); i++) {
            assertEquals(requestBody.getQuality().get(i).getChosenScale().getScale(),
                    responseBody.getQuality().get(i).getChosenScale().getScale());
        }
        assertEquals(requestBody.getState(), responseBody.getState());
        for (int i = 0; i < responseBody.getState().size(); i++) {
            assertEquals(requestBody.getState().get(i).getChosenScale().getScale(),
                    responseBody.getState().get(i).getChosenScale().getScale());
        }

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(1, userAccountRepository.findAll().size());
    }

    @Test
    void saveStateIsUnauthorizedForFailedToken() throws Exception {
        //given
        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/state")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + "FAILED_TOKEN"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        //then
        assertEquals(0, valueCompatibilityAnswersRepository.findAll().size());
    }

    @Test
    void saveStateThrowsExceptionForNullValueCompatibilityAnswersDto() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/state")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(status().is4xxClientError())
                .andExpect(header().exists("messageError"))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains("Required request body is missing"));
    }

    @Test
    void saveStateThrowsExceptionForNullValueCompatibilityAnswersDtoFields() throws Exception {
        //given
        Map<String, Object> populatedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");

        ValueCompatibilityAnswersDto valueCompatibilityAnswersDto = new ValueCompatibilityAnswersDto();
        valueCompatibilityAnswersDto.setQuality(null);

        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/state")
                .content(mapper.writeValueAsString(valueCompatibilityAnswersDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(header().exists("messageError"))
                .andExpect(status().is(400))
                .andReturn();

        //then
        assertTrue(mvcResult.getResponse().getHeader("messageError").contains(env.getProperty("error.ItMustBe15TestsForArea")));
    }


    private void assertDbBeforeSaveQuality(ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity) {
        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());

        Optional<ValueCompatibilityAnswersEntity> valueCompatibilityAnswersEntityOptional =
                valueCompatibilityAnswersRepository.findById(valueCompatibilityAnswersEntity.getId());
        assertTrue(valueCompatibilityAnswersEntityOptional.isPresent());
        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA,
                valueCompatibilityAnswersEntityOptional.get().getUserAnswers().size());
        assertTrue(valueCompatibilityAnswersEntityOptional
                .get()
                .getUserAnswers()
                .stream()
                .allMatch(choice -> choice.getArea() == Area.GOAL));
    }

    private void assertDbBeforeSaveState(ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity) {
        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());

        Optional<ValueCompatibilityAnswersEntity> valueCompatibilityAnswersEntityOptional =
                valueCompatibilityAnswersRepository.findById(valueCompatibilityAnswersEntity.getId());
        assertTrue(valueCompatibilityAnswersEntityOptional.isPresent());
        assertEquals(TOTAL_NUMBER_OF_QUESTIONS_FOR_AREA * 2,
                valueCompatibilityAnswersEntityOptional.get().getUserAnswers().size());
        assertTrue(valueCompatibilityAnswersEntityOptional
                .get()
                .getUserAnswers()
                .stream()
                .allMatch(choice -> choice.getArea() == Area.GOAL || choice.getArea() == Area.QUALITY));
    }


    // ============================= /test/goal  &&  /test/quality  &&  /test/state  all together ======================
    @Test
    void saveValueCompatibilityAnswersSuccessForNullToken() throws Exception {
        //given
        ValueCompatibilityAnswersDto requestBody = getValueCompatibilityAnswersDto(env);


        // GOAL
        //when
        MvcResult mvcResult = mockMvc.perform(post("/test/goal")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("AUTHORIZATION"))
                .andReturn();

        //then
        ValueCompatibilityAnswersDto responseBody = mapper.readValue(mvcResult
                        .getResponse()
                        .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(requestBody.getGoal(), responseBody.getGoal());
        for (int i = 0; i < responseBody.getGoal().size(); i++) {
            assertEquals(requestBody.getGoal().get(i).getChosenScale().getScale(),
                    responseBody.getGoal().get(i).getChosenScale().getScale());
        }

        ObjectId goalUserId = responseBody.getUserId();
        String token = mvcResult.getResponse().getHeader("AUTHORIZATION");

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());

        // QUALITY
        //when
        mvcResult = mockMvc.perform(post("/test/quality")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", token))
                .andExpect(status().isOk())
                .andReturn();

        //then
        responseBody = mapper.readValue(mvcResult
                        .getResponse()
                        .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(responseBody.getQuality(), requestBody.getQuality());
        for (int i = 0; i < responseBody.getQuality().size(); i++) {
            assertEquals(requestBody.getQuality().get(i).getChosenScale().getScale(),
                    responseBody.getQuality().get(i).getChosenScale().getScale());
        }
        ObjectId qualityUserId = responseBody.getUserId();

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());


        // STATE
        //when
        mvcResult = mockMvc.perform(post("/test/state")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(requestBody))
                .header("AUTHORIZATION", token))
                .andExpect(status().isOk())
                .andReturn();

        //then
        responseBody = mapper.readValue(mvcResult
                        .getResponse()
                        .getContentAsString(), ValueCompatibilityAnswersDto.class);

        assertEquals(requestBody.getState(), responseBody.getState());
        for (int i = 0; i < responseBody.getState().size(); i++) {
            assertEquals(requestBody.getState().get(i).getChosenScale().getScale(),
                    responseBody.getState().get(i).getChosenScale().getScale());
        }

        ObjectId stateUserId = responseBody.getUserId();

        assertEquals(1, userRepository.findAll().size());
        assertEquals(1, credentialsRepository.findAll().size());
        assertEquals(1, tokenRepository.findAll().size());
        assertEquals(1, valueCompatibilityAnswersRepository.findAll().size());
        assertEquals(goalUserId, qualityUserId);
        assertEquals(goalUserId, stateUserId);

    }


    // ======================================== /test/generateTokenList ================================================
    // ========================== ResponseEntity<List<String>> generateInviteTokenList() ===============================
    @Test
    void generateInviteTokenListSuccess() throws Exception {
        // given
        Map<String, Object> populatedObjects = populateDbWithAnonimUserAndCredentialsAndToken();
        TokenEntity tokenEntity = (TokenEntity) populatedObjects.get("tokenEntity");
        User user = (User) populatedObjects.get("user");

        // when
        MvcResult mvcResult = mockMvc.perform(get("/test/generateTokenList")
                .header("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<String> responseBody = mapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);
        User updatedUser = userRepository.findById(user.getId()).get();

        assertTrue(updatedUser
                .getUsersForMatchingId()
                .stream()
                .allMatch(userId ->
                        responseBody.contains(tokenRepository.findByUserId(userId)
                                .get()
                                .getToken()) &&
                        userRepository.findById(userId)
                                .get()
                                .getUsersForMatchingId()
                                .get(0).equals(updatedUser.getId())));
    }


    // =========================================== private =============================================================
    /**
     * Populates db with partial {@link ValueCompatibilityAnswersEntity} for given user to test entities
     * that need ValueCompatibilityEntities in db with GOAL or GOAL and QUALITY (or GOAL and STATE) {@link Choice}.
     * Before calling this method, make sure that the list of choices contains only the data necessary for saving.
     * In this method, all data will be inserted into the database.
     * @param choices must not be {@literal null}.
     * @param user must not be {@literal null}.
     * @return new created {@link ValueCompatibilityAnswersEntity} with choices.
     */
    private ValueCompatibilityAnswersEntity populateDbWithPartialValueCompatibilityAnswersEntity(List<Choice> choices, User user, Area area) {
        ValueCompatibilityAnswersEntity answersEntity = new ValueCompatibilityAnswersEntity();
        answersEntity.setUserId(user.getId());
        if(area == Area.GOAL) {
            answersEntity.setUserAnswers(choices.stream().filter(choice -> choice.getArea() == Area.GOAL).collect(Collectors.toList()));
        } else if (area == Area.QUALITY) {
            answersEntity.setUserAnswers(choices.stream().filter(choice -> choice.getArea() == Area.GOAL || choice.getArea() == Area.QUALITY).collect(Collectors.toList()));
        } else {
            answersEntity.setUserAnswers(choices.stream().filter(choice -> choice.getArea() == Area.GOAL || choice.getArea() == Area.STATE).collect(Collectors.toList()));
        }
        answersEntity.setPassDate(LocalDateTime.now());
        answersEntity.setCreationDate(LocalDateTime.now());
        answersEntity.setPassed(false);
        return valueCompatibilityAnswersRepository.insert(answersEntity);
    }

}

