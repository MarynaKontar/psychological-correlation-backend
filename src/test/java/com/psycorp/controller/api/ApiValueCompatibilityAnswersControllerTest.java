package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psycorp.configuration.SecurityConfig;
import com.psycorp.model.dto.ValueCompatibilityAnswersDto;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.security.UserDetailsServiceImpl;
import com.psycorp.security.token.AuthorisationToken;
import com.psycorp.security.token.TokenAuthFilter;
import com.psycorp.security.token.TokenAuthProvider;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.security.SecurityConstant;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.ValueProfileService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.ChoiceDtoConverter;
import com.psycorp.сonverter.UserDtoConverter;
import com.psycorp.сonverter.ValueCompatibilityAnswersDtoConverter;
import com.psycorp.сonverter.ValueProfileIndividualDtoConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
//@WebMvcTest(controllers = {ApiValueCompatibilityAnswersController.class})
////@Import({TokenAuthProvider.class, AuthorisationToken.class,
////        TokenAuthFilter.class, TokenPrincipal.class,
////        SecurityConstant.class, UserDetailsServiceImpl.class,
////        TokenRepository.class})
////@ContextConfiguration
////@WebAppConfiguration
////@AutoConfigureMockMvc(secure = false)
//@Import(SecurityConfig.class)
public class ApiValueCompatibilityAnswersControllerTest {

//     mockMvc.perform(MockMvcRequestBuilders.
//             post("/muffins")
//             .content(convertObjectToJsonString(muffin))
//            .contentType(MediaType.APPLICATION_JSON)
//        .accept(MediaType.APPLICATION_JSON))
//            .andExpect(MockMvcResutMatchers.status().isCreated())
//            .andExpect(MockMvcResutMatchers.content().json(convertObjectToJsonString(muffin)));

//    @Autowired
//    private WebApplicationContext context;
////    @Autowired
////    private ApiValueCompatibilityAnswersController apiValueCompatibilityAnswersController;
//    @Autowired
//    private MockMvc mockMvc;
//    @MockBean
//    private ValueCompatibilityAnswersService valueCompatibilityAnswersService;
//    @MockBean
//    private ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter;
//    @MockBean
//    private TokenService tokenService;
//    @MockBean
//    private ValueProfileService valueProfileService;
//    @MockBean
//    private ValueProfileIndividualDtoConverter valueProfileIndividualDtoConverter;
//    @MockBean
//    private ChoiceDtoConverter choiceDtoConverter;
//    @MockBean
//    private UserDtoConverter userDtoConverter;

    @InjectMocks
    private ApiValueCompatibilityAnswersController apiValueCompatibilityAnswersController;
    private MockMvc mockMvc;
    @Mock
    private ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    @Mock
    private ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter;
    @Mock
    private TokenService tokenService;
    @Mock
    private ValueProfileService valueProfileService;
    @Mock
    private ValueProfileIndividualDtoConverter valueProfileIndividualDtoConverter;
    @Mock
    private ChoiceDtoConverter choiceDtoConverter;
    @Mock
    private UserDtoConverter userDtoConverter;

//    @MockBean
//    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(apiValueCompatibilityAnswersController)
//                .webAppContextSetup(context)
//                .apply(springSecurity())
                .build();

        Fixture.of(ValueCompatibilityAnswersEntity.class).addTemplate("valid", new Rule(){{
            add("id", null);
            add("userId", null);
            add("creationDate", null);
            add("passDate", null);
            add("passed", null);
            add("userAnswers", null);
//            add("userAnswers", one(Choice.class, "valid"));
        }});

        Fixture.of(ValueCompatibilityAnswersDto.class).addTemplate("valid", new Rule(){{
            add("id", null);
            add("userId", null);
            add("passDate", null);
            add("passed", null);
            add("goal", null);
            add("quality", null);
            add("state", null);
        }});
    }

    @Test
    public void getInitValueCompatibilityAnswersSuccess() throws Exception {

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = Fixture.from(ValueCompatibilityAnswersEntity.class).gimme("valid");
        ValueCompatibilityAnswersDto valueCompatibilityAnswersDto = Fixture.from(ValueCompatibilityAnswersDto.class).gimme("valid");
        when(valueCompatibilityAnswersService.getInitValueCompatibilityAnswers()).thenReturn(valueCompatibilityAnswersEntity);
        when(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersEntity)).thenReturn(valueCompatibilityAnswersDto);

        MvcResult mvcResult = mockMvc.perform(get("/test/initTest"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        ValueCompatibilityAnswersDto actualValueCompatibilityAnswersDto = mapper
                .readValue(mvcResult.getResponse().getContentAsString(), ValueCompatibilityAnswersDto.class);

        ValueCompatibilityAnswersDto expectedResponseBody = valueCompatibilityAnswersDto;

        assertEquals(actualValueCompatibilityAnswersDto, expectedResponseBody);
        verify(valueCompatibilityAnswersService, times(1)).getInitValueCompatibilityAnswers();
        verify(valueCompatibilityAnswersDtoConverter, times(1)).transform(valueCompatibilityAnswersEntity);
        verifyNoMoreInteractions(valueCompatibilityAnswersService, valueCompatibilityAnswersDtoConverter);
    }

    @Test
    public void saveGoal() {
    }

    @Test
    public void saveQuality() {
    }

    @Test
    public void saveState() {
    }

    @Test
    public void generateInviteTokenList() throws Exception {

        List<String> tokenList = new ArrayList<>(Arrays.asList("token1", "token2", "token3"));
        when(tokenService.generateInviteTokenList(3)).thenReturn(tokenList);

        MvcResult mvcResult = mockMvc.perform(get("/test/generateTokenList"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        List<String> responseBody = mapper.readValue(mvcResult.getResponse().getContentAsString(), List.class);

        assertEquals(responseBody, tokenList);
        verify(tokenService, times(1)).generateInviteTokenList(3);
//        verifyNoMoreInteractions(tokenService, );
    }

    @Test
    public void getValueProfile() {
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }
}