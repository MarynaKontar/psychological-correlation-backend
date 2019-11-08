package com.psycorp.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psycorp.PopulateDb;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract class for integration tests of services.
 * Repository layer uses only for populate and clear db.
 * Use not embedded mongo database described in application-test.yml
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractServiceIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    CredentialsRepository credentialsRepository;
    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired
    ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    @Autowired
    UserMatchRepository userMatchRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    Environment env;

    @Autowired
    PopulateDb populateDb;

    /**
     * Checks if all collections is empty.
     */
    @BeforeEach
    void start() {
        assertEquals(0, mongoTemplate.findAll(User.class).size());
        assertEquals(0, mongoTemplate.findAll(CredentialsEntity.class).size());
        assertEquals(0, mongoTemplate.findAll(TokenEntity.class).size());
        assertEquals(0, mongoTemplate.findAll(ValueCompatibilityAnswersEntity.class).size());
        assertEquals(0, mongoTemplate.findAll(UserAccountEntity.class).size());
        assertEquals(0, mongoTemplate.findAll(UserMatchEntity.class).size());
    }

    /**
     * Clears db after each test.
     */
    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(UserAccountEntity.class);
        mongoTemplate.dropCollection(CredentialsEntity.class);
        mongoTemplate.dropCollection(TokenEntity.class);
        mongoTemplate.dropCollection(ValueCompatibilityAnswersEntity.class);
        mongoTemplate.dropCollection(UserMatchEntity.class);
        mongoTemplate.getDb().watch();
    }

}
