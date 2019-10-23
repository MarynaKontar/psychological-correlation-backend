package com.psycorp;


import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.psycorp.model.dto.ChangePasswordDto;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.ValueCompatibilityAnswersDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.UserRole;
import org.bson.types.ObjectId;

/**
 * Utils class for creation of fixture for test objects
 */
public class FixtureObjectsForTest {
    private static final String NAME = "userName";
    private static final String EMAIL = "email@gmail.com";
    private static final Gender GENDER = Gender.FEMALE;
    private static final Integer AGE = 35;
    private static final UserRole USER_ROLE = UserRole.USER;
    private static final UserRole ANONIM_ROLE = UserRole.ANONIM;
    public static final String ANONIM_NAME = "anonimName";
    private static final String PASSWORD = "somePassword";

    public static void fixtureRegisteredUser() {
        Fixture.of(User.class).addTemplate("user", new Rule() {{
            add("name", NAME);
            add("email", EMAIL);
            add("gender", GENDER);
            add("age", AGE);
            add("role", USER_ROLE);
        }});
    }

    public static void fixtureAnonimUser() {
        Fixture.of(User.class).addTemplate("anonimUser", new Rule() {{
            add("name", ANONIM_NAME);
            add("role", ANONIM_ROLE);
        }});
    }

    public static void fixtureCredentialsDto() {
        Fixture.of(CredentialsDto.class).addTemplate("credentialsRegistrationDto", new Rule() {{
            add("name", NAME);
            add("email", EMAIL);
            add("password", PASSWORD);
            add("gender", GENDER);
            add("age", AGE);
        }});
    }

    public static void fixtureSimpleUserDto() {
        Fixture.of(SimpleUserDto.class).addTemplate("simpleUserDto", new Rule() {{
            add("name", NAME);
            add("email", EMAIL);
            add("gender", GENDER);
            add("age", AGE);
        }});
    }

    public static void fixtureIncompleteSimpleUserDto() {
        Fixture.of(SimpleUserDto.class).addTemplate("incompleteSimpleUserDto", new Rule() {{
            add("name", NAME);
            add("gender", GENDER);
            add("age", AGE);
        }});
    }

    public static void fixtureMissingIncompleteSimpleUserDto() {
        Fixture.of(SimpleUserDto.class).addTemplate("incompleteSimpleUserDtoNullName", new Rule() {{
            add("name", null);
            add("gender", GENDER);
            add("age", AGE);
        }});
        Fixture.of(SimpleUserDto.class).addTemplate("incompleteSimpleUserDtoNullGender", new Rule() {{
            add("name", NAME);
            add("gender", null);
            add("age", AGE);
        }});
        Fixture.of(SimpleUserDto.class).addTemplate("incompleteSimpleUserDtoNullAge", new Rule() {{
            add("name", NAME);
            add("gender", GENDER);
            add("age", null);
        }});
    }

    public static void fixtureChangePasswordDto(String userPassword, String newPassword) {
        Fixture.of(ChangePasswordDto.class).addTemplate("changePasswordDto", new Rule() {{
            add("oldPassword", userPassword);
            add("newPassword", newPassword);
        }});
    }

    public static void fixtureMissingCredentialsDto() {
        Fixture.of(CredentialsDto.class).addTemplate("credentialsDtoNullName", new Rule() {{
            add("name", null);
            add("email", EMAIL);
            add("password", PASSWORD);
            add("gender", GENDER);
            add("age", AGE);
        }});
        Fixture.of(CredentialsDto.class).addTemplate("credentialsDtoNullEmail", new Rule() {{
            add("name", NAME);
            add("email", null);
            add("password", PASSWORD);
            add("gender", GENDER);
            add("age", AGE);
        }});
        Fixture.of(CredentialsDto.class).addTemplate("credentialsDtoNullPassword", new Rule() {{
            add("name", NAME);
            add("email", EMAIL);
            add("password", null);
            add("gender", GENDER);
            add("age", AGE);
        }});
        Fixture.of(CredentialsDto.class).addTemplate("credentialsDtoNullGender", new Rule() {{
            add("name", NAME);
            add("email", EMAIL);
            add("password", PASSWORD);
            add("gender", null);
            add("age", AGE);
        }});
        Fixture.of(CredentialsDto.class).addTemplate("credentialsDtoNullAge", new Rule() {{
            add("name", NAME);
            add("email", EMAIL);
            add("password", PASSWORD);
            add("gender", GENDER);
            add("age", null);
        }});
    }

    public static void fixtureValueCompatibilityAnswersEntity() {
         Fixture.of(ValueCompatibilityAnswersEntity.class).addTemplate("valueCompatibilityAnswersEntity", new Rule(){{
            add("id", null);
            add("userId", null);
            add("creationDate", null);
            add("passDate", null);
            add("passed", null);
            add("userAnswers", null);
    //            add("userAnswers", one(Choice.class, "valid"));
        }});
    }

    public static void fixtureValueCompatibilityAnswersDto() {
        Fixture.of(ValueCompatibilityAnswersDto.class).addTemplate("valueCompatibilityAnswersDto", new Rule() {{
            add("id", null);
            add("userId", null);
            add("passDate", null);
            add("passed", null);
            add("goal", null);
            add("quality", null);
            add("state", null);
        }});
    }
}
