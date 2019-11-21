package com.psycorp;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.ValueCompatibilityAnswersDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.*;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.implementation.UserServiceImplIntegrationTest;
import com.psycorp.сonverter.ValueCompatibilityAnswersDtoConverter;
import org.bson.types.ObjectId;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static com.psycorp.FixtureObjectsForTest.*;

/**
 * Utils class for creation of test objects
 */
public class ObjectsForTests {
//    Utility class, that doesn't have instances.
    private ObjectsForTests() {  throw new AssertionError("You cann't create instance of ObjectsForTests class, because it's utility class.");  }

    public static Optional<UserAccountEntity> getUserAccountEntity(ObjectId id,
                                                                   ObjectId userId,
                                                                   List<ObjectId> usersWhoInvitedYouId,
                                                                   List<ObjectId> usersWhoYouInviteId) {
        fixtureUserAccountEntity(id, userId, usersWhoInvitedYouId, usersWhoYouInviteId, "userAccountEntity");
        return Optional.of(Fixture.from(UserAccountEntity.class).gimme("userAccountEntity"));
    }

    public static UserAccountEntity getShortUserAccountEntity(ObjectId userId) {
        UserAccountEntity userAccountEntity = new UserAccountEntity();
        userAccountEntity.setUserId(userId);
        userAccountEntity.setAccountType(AccountType.OPEN);
        return userAccountEntity;
    }

    public static UserAccountEntity getUserAccountEntity(ObjectId userId, List<ObjectId> usersWhoInvitedYouId, List<ObjectId> usersWhoYouInviteId) {
        UserAccountEntity userAccountEntity = new UserAccountEntity();
        userAccountEntity.setUserId(userId);
        userAccountEntity.setAccountType(AccountType.OPEN);
        userAccountEntity.setUsersWhoInvitedYouId(usersWhoInvitedYouId);
        userAccountEntity.setUsersWhoYouInviteId(usersWhoYouInviteId);
        return userAccountEntity;
    }

    public static User getRegisteredUser(ObjectId userId, String name, List<ObjectId> usersForMatchingId, String label) {
        fixtureRegisteredUser(userId, name, usersForMatchingId, label);
        return Fixture.from(User.class).gimme(label == null ? "user" : label);
    }

    public static User getAnonimUser() {
        User user = new User();
        user.setName(UUID.randomUUID().toString());
        user.setRole(UserRole.ANONIM);
        return user;
    }
    public static User getAnonimUser(ObjectId userId, String name, List<ObjectId> usersForMatchingId, String label) {
        fixtureAnonimUser(userId, name, usersForMatchingId, label);
        return Fixture.from(User.class).gimme(label);
    }

    public static User getIncompleteUser() {
        User user = new User();
        user.setName("nameForIncompleteUser");
        user.setGender(Gender.FEMALE);
        user.setAge(38);
        return user;
    }

    public enum UserField {
        NAME,
        GENDER,
        AGE;
    }
    public static User getNotValidIncompleteUser(UserField notValidUserField) {
        User notValidUser = new User();
        notValidUser.setName(notValidUserField.equals(UserField.NAME) ? null : "name");
        notValidUser.setGender(notValidUserField.equals(UserField.GENDER) ? null : Gender.FEMALE);
        notValidUser.setAge(notValidUserField.equals(UserField.AGE) ? null : 35);
        return notValidUser;
    }

    public static CredentialsEntity getCredentialsEntity(User user, String password) {
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(user.getId());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(password != null) { credentialsEntity.setPassword(passwordEncoder.encode(password)); }
        return credentialsEntity;
    }

    public static ValueCompatibilityAnswersEntity getValueCompatibilityEntity() {
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = new ValueCompatibilityAnswersEntity();
        valueCompatibilityAnswersEntity.setUserAnswers(choiceList());
        valueCompatibilityAnswersEntity.setCreationDate(LocalDateTime.of(2019, 10, 19, 15, 50, 55));
        valueCompatibilityAnswersEntity.setPassed(false);
        return valueCompatibilityAnswersEntity;
    }

    public static TokenEntity getTokenEntity(ObjectId tokenId, ObjectId userId, TokenType tokenType, String token) {
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setId(tokenId);
        tokenEntity.setType(tokenType);
        tokenEntity.setToken(token);
        tokenEntity.setUserId(userId);
        tokenEntity.setExpirationDate(LocalDateTime.now().plusDays(1));
        return tokenEntity;
    }

    public static ValueCompatibilityAnswersDto getValueCompatibilityAnswersDto(Environment env) {
        ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter = new ValueCompatibilityAnswersDtoConverter(env);
        return valueCompatibilityAnswersDtoConverter.transform(getValueCompatibilityEntity());
    }

    public static TokenPrincipal getTokenPrincipal(User user) {
        TokenPrincipal tokenPrincipal = new TokenPrincipal();
        tokenPrincipal.setId(user.getId());
        tokenPrincipal.setUsername(user.getName());
        tokenPrincipal.setRole(user.getRole());
        tokenPrincipal.setPassword(null);
        return tokenPrincipal;
    }

    public static SimpleUserDto getSimpleUserDtoForCreatedAnonimUser(ObjectId userId) {
        Fixture.of(SimpleUserDto.class).addTemplate("simpleUserDtoForAnonimUser", new Rule() {{
            add("id", userId);
            add("name", ANONIM_NAME);
        }});
       return Fixture.from(SimpleUserDto.class).gimme("simpleUserDtoForAnonimUser");
    }


    /**
     * Returns list of {@link Choice} with three {@link Area}: GOAL, QUALITY and STATE
     * for six {@link Scale}.
     * All firstScales, secondScales and chosenScales are filled.
     * @return list of {@link Choice}.
     */
    private static List<Choice> choiceList(){

        //GOAL
        List<Choice> choiceGoal = new ArrayList<>();
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.TWO, 0));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.THREE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.FOUR, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.FIVE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.SIX, 0));

        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.THREE, 0));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.FOUR, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.FIVE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.SIX, 0));

        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.FOUR, 0));
        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.FIVE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.SIX, 0));

        choiceGoal.add(getChoice(Area.GOAL, Scale.FOUR, Scale.FIVE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.FOUR, Scale.SIX, 0));

        choiceGoal.add(getChoice(Area.GOAL, Scale.FIVE, Scale.SIX, 0));


        //QUALITY
        List<Choice> choiceQuality = new ArrayList<>();
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.TWO, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.THREE,  1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.FOUR, 1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.FIVE, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.SIX, 0));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.THREE, 1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.FOUR, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.FIVE, 1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.SIX, 1));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.FOUR, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.FIVE, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.SIX, 0));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.FOUR, Scale.FIVE, 1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.FOUR, Scale.SIX, 1));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.FIVE, Scale.SIX, 0));


        //STATE
        List<Choice> choiceState = new ArrayList<>();
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.TWO, 1));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.THREE, 1));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.FOUR, 0));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.FIVE, 1));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.SIX, 1));

        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.THREE, 0));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.FOUR, 1));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.FIVE, 0));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.SIX, 1));

        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.FOUR, 1));
        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.FIVE, 1));
        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.SIX, 1));

        choiceState.add(getChoice(Area.STATE, Scale.FOUR, Scale.FIVE, 0));
        choiceState.add(getChoice(Area.STATE, Scale.FOUR, Scale.SIX, 1));

        choiceState.add(getChoice(Area.STATE, Scale.FIVE, Scale.SIX, 0));


        List<Choice> choices = new ArrayList<>(choiceGoal);
        choices.addAll(choiceQuality);
        choices.addAll(choiceState);

        return choices;
    }

    /**
     * Gets choice for given area.
     * @param area must not be {@literal null}.
     * @param scaleOne must not be {@literal null}.
     * @param scaleTwo must not be {@literal null}.
     * @return {@link Choice} for given area.
     */
    private static Choice getChoice(Area area, Scale scaleOne, Scale scaleTwo, Integer chosenScaleNumber) {
        if (chosenScaleNumber != 0 && chosenScaleNumber != 1) { throw new IllegalArgumentException("chosenScaleNumber must be 0 or 1, but it is: " + chosenScaleNumber);}
        Choice choice = new Choice();
        choice.setArea(area);

        List<Scale> scales = Arrays.asList(scaleOne, scaleTwo);

        choice.setFirstScale(scales.get(0));
        choice.setSecondScale(scales.get(1));
        choice.setChosenScale(chosenScaleNumber == 0 ? choice.getFirstScale() : choice.getSecondScale());

        return choice;
    }

    public static String getValueCompatibilityAnswersDtoJson() {
        return "{\n" +
                "   \n" +
                "    \"goal\": [\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                 \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "           }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"quality\": [],\n" +
                "    \"state\": []\n" +
                "}";

    }
}
