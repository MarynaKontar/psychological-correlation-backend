package com.psycorp;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.TokenEntity;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

//public class InitEntitiesUtil {
//    public long idConstant = 15478;
//    public ObjectId id = new ObjectId(new Date(idConstant), 101);
//    public ObjectId userId = new ObjectId(new Date(idConstant), 202);
//    public ObjectId userId1, userId2, userId3, userId4, userId5, userId6, userId7, tokenEntityId;
//    public User user, user1, user2, user3, user4, user5, user6, user7;
//    public User userWithNullName, userWithNullAge, userWithNullGender;
//    public TokenEntity tokenEntity;
//    public Optional<UserAccountEntity> userAccountEntityOptional;
//    public UserAccountEntity userAccountEntityForInsertion;
//
//    public void prepareUsersForTests() {
//
//        userId1 = new ObjectId(new Date(idConstant), 1);
//        userId2 = new ObjectId(new Date(idConstant), 2);
//        userId3 = new ObjectId(new Date(idConstant), 3);
//        userId4 = new ObjectId(new Date(idConstant), 4);
//        userId5 = new ObjectId(new Date(idConstant), 5);
//        userId6 = new ObjectId(new Date(idConstant), 6);
//        userId7 = new ObjectId(new Date(idConstant), 7);
//
//        Fixture.of(User.class).addTemplate("user1", new Rule() {{
//            add("id", userId1);
//            add("name", "userName1");
//            add("email", "email1@gmail.com");
//            add("age", 45);
//            add("gender", Gender.MALE);
//            add("role", UserRole.USER);
//            add("usersForMatchingId", Arrays.asList(userId));
//        }});
//        Fixture.of(User.class).addTemplate("user2", new Rule() {{
//            add("id", userId2);
//            add("name", "userName2");
//            add("email", "email2@gmail.com");
//            add("age", 38);
//            add("gender", Gender.MALE);
//            add("role", UserRole.USER);
//        }});
//        Fixture.of(User.class).addTemplate("user3", new Rule() {{
//            add("id", userId3);
//            add("name", "userName3");
//            add("email", "email3@gmail.com");
//            add("age", 27);
//            add("gender", Gender.FEMALE);
//            add("role", UserRole.USER);
//            add("usersForMatchingId", Arrays.asList(userId));
//        }});
//        Fixture.of(User.class).addTemplate("user4", new Rule() {{
//            add("id", userId4);
//            add("name", "userName4");
//            add("email", "email4@gmail.com");
//            add("age", 38);
//            add("gender", Gender.FEMALE);
//            add("role", UserRole.USER);
//        }});
//        Fixture.of(User.class).addTemplate("user5", new Rule() {{
//            add("id", userId5);
//            add("name", "userName5");
//            add("email", "email5@gmail.com");
//            add("age", 56);
//            add("gender", Gender.MALE);
//            add("role", UserRole.USER);
//        }});
//        Fixture.of(User.class).addTemplate("user6", new Rule() {{
//            add("id", userId6);
//            add("name", "userName6");
//            add("email", "email6@gmail.com");
//            add("age", 23);
//            add("gender", Gender.FEMALE);
//            add("role", UserRole.USER);
//        }});
//        Fixture.of(User.class).addTemplate("user7", new Rule() {{
//            add("id", userId7);
//            add("name", "userName7");
//            add("email", "email7@gmail.com");
//            add("age", 43);
//            add("gender", Gender.MALE);
//            add("role", UserRole.USER);
//        }});
//
//        Fixture.of(User.class).addTemplate("fullUser", new Rule() {{
//            add("id", userId);
//            add("name", "userName");
//            add("email", "email@gmail.com");
//            add("age", 25);
//            add("gender", Gender.FEMALE);
//            add("role", UserRole.USER);
//            add("usersForMatchingId", Arrays.asList(
//                    userId1,
//                    userId2,
//                    userId3));
//        }});
//
//        user = Fixture.from(User.class).gimme("fullUser");
//        user1 = Fixture.from(User.class).gimme("user1");
//        user2 = Fixture.from(User.class).gimme("user2");
//        user3 = Fixture.from(User.class).gimme("user3");
//        user4 = Fixture.from(User.class).gimme("user4");
//        user5 = Fixture.from(User.class).gimme("user5");
//        user6 = Fixture.from(User.class).gimme("user6");
//        user7 = Fixture.from(User.class).gimme("user7");
//    }
//
//    public void prepareUsersForAddNameAgeAndGenderMethod() {
//        Fixture.of(User.class).addTemplate("userWithNullName", new Rule(){{
//            add("name", null);
//            add("age", 35);
//            add("gender", Gender.FEMALE);
//
//        }});
//        Fixture.of(User.class).addTemplate("userWithNullAge", new Rule(){{
//            add("name", "name");
//            add("age", null);
//            add("gender", Gender.FEMALE);
//
//        }});
//        Fixture.of(User.class).addTemplate("userWithNullGender", new Rule(){{
//            add("name", "name");
//            add("age", 35);
//            add("gender", null);
//        }});
//
//        userWithNullName = Fixture.from(User.class).gimme("userWithNullName");
//        userWithNullAge = Fixture.from(User.class).gimme("userWithNullAge");
//        userWithNullGender = Fixture.from(User.class).gimme("userWithNullGender");
//    }
//
//    public void prepareUserAccountEntityForTests() {
//        Fixture.of(UserAccountEntity.class).addTemplate("valid", new Rule() {{
//            add("id", id);
//            add("userId", userId);
//            add("accountType", AccountType.OPEN);
//            add("usersWhoInvitedYouId", Arrays.asList(
//                    userId4,
//                    userId5));
//            add("usersWhoYouInviteId", Arrays.asList(
//                    userId6,
//                    userId7));
//        }});
//
//        userAccountEntityOptional = Optional.of(Fixture.from(UserAccountEntity.class).gimme("valid"));
//    }
//    public void prepareTokenEntityForTests() {
//        tokenEntityId = new ObjectId(new Date(idConstant), 7);
//        Fixture.of(TokenEntity.class).addTemplate("tokenEntity1", new Rule() {{
//            add("userId", userId);
//            add("type", TokenType.ACCESS_TOKEN);
//            add("token", "someToken");
//        }});
//
//        tokenEntity = Fixture.from(TokenEntity.class).gimme("tokenEntity1");
//    }
//
//    public void prepareUserAccountEntityForInsertion() {
//        Fixture.of(UserAccountEntity.class).addTemplate("insert", new Rule() {{
//            add("userId", userId);
//            add("accountType", AccountType.OPEN);
//        }});
//
//        userAccountEntityForInsertion = Fixture.from(UserAccountEntity.class).gimme("insert");
//    }
//}
