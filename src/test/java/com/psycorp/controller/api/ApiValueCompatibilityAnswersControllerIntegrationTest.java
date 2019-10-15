package com.psycorp.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psycorp.model.dto.ValueCompatibilityAnswersDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Scale;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.сonverter.ValueCompatibilityAnswersDtoConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ApiValueCompatibilityAnswersController}.
 * For server use {@link MockMvc}.
 * Use not embedded mongo database described in application-test.yml
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiValueCompatibilityAnswersControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    @Autowired
    private ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter;

    @Autowired
    ObjectMapper mapper;

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(UserAccountEntity.class);
        mongoTemplate.dropCollection(CredentialsEntity.class);
        mongoTemplate.dropCollection(TokenEntity.class);
        mongoTemplate.dropCollection(ValueCompatibilityAnswersEntity.class);
        mongoTemplate.dropCollection(UserMatchEntity.class);
    }

    @Test
    public void getInitValueCompatibilityAnswersIntegrationTestSuccess() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/test/initTest"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        // map response from string to ValueCompatibilityAnswersDto
        ValueCompatibilityAnswersDto valueCompatibilityAnswersDto = mapper
                .readValue(mvcResult.getResponse().getContentAsString(), ValueCompatibilityAnswersDto.class);

        // assert
        // GOAL
        assertTrue(valueCompatibilityAnswersDto.getGoal().size() == 15);
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
        assertTrue(valueCompatibilityAnswersDto.getQuality().size() == 15);
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
        assertTrue(valueCompatibilityAnswersDto.getState().size() == 15);
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

}
