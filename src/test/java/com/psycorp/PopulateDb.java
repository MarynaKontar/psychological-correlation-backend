package com.psycorp;

import br.com.six2six.fixturefactory.Fixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.psycorp.FixtureObjectsForTest.fixtureAnonimUser;
import static com.psycorp.FixtureObjectsForTest.fixtureRegisteredUser;
import static com.psycorp.ObjectsForTests.getShortUserAccountEntity;
import static com.psycorp.ObjectsForTests.getTokenEntity;

@Component
public class PopulateDb {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserAccountRepository userAccountRepository;
    private final ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    private final UserMatchRepository userMatchRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper;
    private final Environment env;

    @Autowired
    public PopulateDb(UserRepository userRepository,
                      TokenRepository tokenRepository,
                      CredentialsRepository credentialsRepository,
                      UserAccountRepository userAccountRepository,
                      ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository,
                      UserMatchRepository userMatchRepository,
                      PasswordEncoder passwordEncoder,
                      ObjectMapper mapper,
                      Environment env) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.credentialsRepository = credentialsRepository;
        this.userAccountRepository = userAccountRepository;
        this.valueCompatibilityAnswersRepository = valueCompatibilityAnswersRepository;
        this.userMatchRepository = userMatchRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.env = env;
    }

    public User populateDbWithRegisteredUser(String name, Boolean isUsersForMatching) {
        fixtureRegisteredUser(null, name, null);
        User user = Fixture.from(User.class).gimme("user");

        if(isUsersForMatching) {
            ObjectId userId = new ObjectId();
            user.setId(userId);
            fixtureRegisteredUser(null, "userForMatching1For" + name, null);
            User userForMatching1 = Fixture.from(User.class).gimme("user");
            userForMatching1.setUsersForMatchingId(Collections.singletonList(user.getId()));
            userForMatching1 = userRepository.save(userForMatching1);
            TokenEntity tokenEntityForUserForMatching1 = populateDbWithTokenEntity(userForMatching1, TokenType.ACCESS_TOKEN, "someTokenForUserForMatching1");

            fixtureRegisteredUser(null, "userForMatching2For" + name, null);
            User userForMatching2 = Fixture.from(User.class).gimme("user");
            userForMatching2.setUsersForMatchingId(Collections.singletonList(user.getId()));
            userForMatching2 = userRepository.save(userForMatching2);
            TokenEntity tokenEntityForUserForMatching2 = populateDbWithTokenEntity(userForMatching2, TokenType.ACCESS_TOKEN, "someTokenForUserForMatching2");

            user.setUsersForMatchingId(Arrays.asList(userForMatching1.getId(), userForMatching2.getId()));
        }
        return userRepository.save(user);

    }

    public User populateDbWithAnonimUser(String name) {
        if (name != null) { fixtureAnonimUser(null, name, null, null); }
        User user = Fixture.from(User.class).gimme("anonimUser");
        return userRepository.save(user);
    }

    public TokenEntity populateDbWithTokenEntity(User user, TokenType tokenType, String token) {
        TokenEntity tokenEntity = getTokenEntity(null, user.getId(), tokenType, token);
        return tokenRepository.save(tokenEntity);
    }

    public CredentialsEntity populateDbWithCredentialsEntity(User user, String password) {
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(user.getId());
        if(password != null) { credentialsEntity.setPassword(passwordEncoder.encode(password)); }
        return credentialsRepository.save(credentialsEntity);
    }

    public UserAccountEntity populateDbWithShortUserAccountEntity(ObjectId userId) {
        UserAccountEntity userAccountEntity = getShortUserAccountEntity(userId);
        return userAccountRepository.insert(userAccountEntity);
    }

    public Map<String, Object> populateDbWithAnonimUserAndCredentialsAndToken() {
        return populateDbWithAnonimUserAndCredentialsAndToken("anonimName", null);
    }

    public Map<String, Object> populateDbWithAnonimUserAndCredentialsAndToken(String name, String token) {

        // add to db anonim user, credentialsEntity and tokenEntity for it
        User user = populateDbWithAnonimUser(name);
        CredentialsEntity credentialsEntity = populateDbWithCredentialsEntity(user, null);
        TokenEntity tokenEntity = populateDbWithTokenEntity(user, TokenType.ACCESS_TOKEN, token == null ? "someTokenForAnonimUser" : token);

        Map<String, Object> preparedObjects = new HashMap<>(3);
        preparedObjects.put("user", user);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        preparedObjects.put("tokenEntity", tokenEntity);

        return preparedObjects;
    }

    public Map<String, Object> populateDbWithRegisteredUserAndCredentialsAndUserAccountAndToken(Boolean isUsersForMatching) {
        User user = populateDbWithRegisteredUser(null, isUsersForMatching);
        TokenEntity tokenEntity = populateDbWithTokenEntity(user, TokenType.ACCESS_TOKEN, "someTokenForRegisteredUser");
        CredentialsEntity credentialsEntity = populateDbWithCredentialsEntity(user, "oldPassword");
        UserAccountEntity userAccountEntity = populateDbWithShortUserAccountEntity(user.getId());

        Map<String, Object> preparedObjects = new HashMap<>(4);
        preparedObjects.put("user", user);
        preparedObjects.put("tokenEntity", tokenEntity);
        preparedObjects.put("credentialsEntity", credentialsEntity);
        preparedObjects.put("userAccountEntity", userAccountEntity);
        return preparedObjects;
    }

    /**
     * Populates db with partial or full {@link ValueCompatibilityAnswersEntity} for given user to test entities that needs
     * ValueCompatibilityEntities in db with GOAL, or  GOAL and QUALITY (or GOAL and STATE), or full list of {@link Choice}.
     * Before calling this method, make sure that the list of choices contains only the data necessary for saving.
     * In this method, all data will be inserted into the database.
     * @param choices must not be {@literal null}.
     * @param user must not be {@literal null}.
     * @return new created {@link ValueCompatibilityAnswersEntity} with choices.
     */
    public ValueCompatibilityAnswersEntity populateDbWithValueCompatibilityAnswersEntity(List<Choice> choices, User user, Area area) {
        ValueCompatibilityAnswersEntity answersEntity = new ValueCompatibilityAnswersEntity();
        answersEntity.setUserId(user.getId());
        switch (area) {
            case GOAL:
                answersEntity.setUserAnswers(choices
                        .stream()
                        .filter(choice -> choice.getArea() == Area.GOAL)
                        .collect(Collectors.toList()));
                break;
            case QUALITY:
                answersEntity.setUserAnswers(choices
                        .stream()
                        .filter(choice -> choice.getArea() == Area.GOAL ||
                                choice.getArea() == Area.QUALITY)
                        .collect(Collectors.toList()));
                break;
            case STATE:
                answersEntity.setUserAnswers(choices
                        .stream()
                        .filter(choice -> choice.getArea() == Area.GOAL ||
                                choice.getArea() == Area.STATE)
                        .collect(Collectors.toList()));
                break;
            case TOTAL:
                answersEntity.setUserAnswers(choices);
                break;
        }
        answersEntity.setPassDate(LocalDateTime.now());
        answersEntity.setCreationDate(LocalDateTime.now());

        if (area == Area.TOTAL) {
            answersEntity.setPassed(true);
        } else answersEntity.setPassed(false);

        return valueCompatibilityAnswersRepository.insert(answersEntity);
    }
}
