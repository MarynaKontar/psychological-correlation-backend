package com.psycorp.controller.api;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.objects.Credentials;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.security.token.TokenAuthFilter;
import com.psycorp.service.UserService;
import com.psycorp.service.security.TokenService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MongoTemplate mongoTemplate;
    @MockBean
    private UserService userService;
    @Autowired
    TokenService tokenService;

    private long idConstant = 15478;
    private ObjectId id = new ObjectId(new Date(idConstant), 101);
    private ObjectId userId = new ObjectId(new Date(idConstant), 202);
    private User user;

    private MongoCollection userCollection;
    private MongoCollection credentialsEntityCollection;
    private MongoCollection tokenEntityCollection;
    private MongoCollection valueCompatibilityAnswersEntityCollection;
    private MongoCollection userMatchEntityCollection;

    @Autowired
    private TokenAuthFilter tokenAuthFilter;

    @BeforeEach
    void setUp() {
//        MockitoAnnotations.initMocks(this);
//        mockMvc = MockMvcBuilders
//                .standaloneSetup(apiUserController)
//                .addFilters(tokenAuthFilter)
//                .build();
        userCollection = mongoTemplate.getCollection("user");
        credentialsEntityCollection = mongoTemplate.getCollection("credentialsEntity");
        tokenEntityCollection = mongoTemplate.getCollection("tokenEntity");
        valueCompatibilityAnswersEntityCollection = mongoTemplate.getCollection("valueCompatibilityAnswersEntity");
        userMatchEntityCollection = mongoTemplate.getCollection("userMatchEntity");
    }

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
    void incompleteRegistration() throws Exception {
        User anonimUser = userService.createAnonimUser();
        TokenEntity tokenEntity = tokenService.generateAccessTokenForAnonim(anonimUser);
        String token = tokenEntity.getToken();

        User incompleteUser = new User();
        incompleteUser.setId(anonimUser.getId());
        incompleteUser.setAge(45);
        incompleteUser.setGender(Gender.FEMALE);
        incompleteUser.setName("name");
        incompleteUser.setEmail("email@gmail.com");

        SimpleUserDto simpleUserDto = new SimpleUserDto();
        simpleUserDto.setId(incompleteUser.getId());
        simpleUserDto.setName(incompleteUser.getName());
        simpleUserDto.setAge(incompleteUser.getAge());
        simpleUserDto.setGender(incompleteUser.getGender());
        simpleUserDto.setEmail(incompleteUser.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", token);
        String json = asJsonString(simpleUserDto);

        this.mockMvc.perform(post("/incompleteRegistration")
                .content(json)
                .header("Content-Type", "application/json")
                .header("AUTHORIZATION", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    private void prepareUsersForTests() {
        Fixture.of(User.class).addTemplate("anonimUser", new Rule() {{
            add("id", userId);
            add("name", "userName");
            add("role", UserRole.ANONIM);
        }});

        user = Fixture.from(User.class).gimme("anonimUser");
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
