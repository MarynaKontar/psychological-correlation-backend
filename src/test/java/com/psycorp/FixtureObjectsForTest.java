package com.psycorp;


import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.psycorp.model.dto.ChangePasswordDto;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.UserRole;

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
    private static final String ANONIM_NAME = "anonimName";
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

}
