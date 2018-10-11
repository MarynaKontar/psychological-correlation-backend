package com.psycorp;

import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.entity.UserMatch;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.util.Entity;
import com.psycorp.сonverter.UserAnswersDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
//@PropertySource(value = {"classpath:scales/scalesukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
public class TestDb {

//    private final Environment env;
//
//    private final UserAnswersService userAnswersService;
//    private final UserService userService;
//    private final UserMatchService userMatchService;
//
//    private User user1 = new User();
//    private User user2 = new User();
//    private User user3 = new User();
//
//
//    @Autowired
//    public TestDb(Environment env, UserAnswersService userAnswersService, UserService userService,
//                  UserMatchService userMatchService) {
//        this.env = env;
//        this.userAnswersService = userAnswersService;
//        this.userService = userService;
//        this.userMatchService = userMatchService;
//    }

    //for testing. Delete in production
//    @PostConstruct
//    public void createInitEntities() {
//
//        user1.setName("user6");
//        user1.setEmail("email6");
//        user1 = userService.createUser(user1);
//
//        UserAnswers userAnswers1 = Entity.createRandomUserAnswers(user1);
//        userAnswersService.createUser(userAnswers1);
//
//        user2.setName("user7");
//        user2.setEmail("email7");
//        user2 = userService.createUser(user2);
//
//        UserAnswers userAnswers2 = Entity.createRandomUserAnswers(user2);
//        userAnswersService.createUser(userAnswers2);
//
//
//        user3.setName("user8");
//        user3.setEmail("email8");
//        user3 = userService.createUser(user3);
//
//        UserAnswers userAnswers3 = Entity.createRandomUserAnswers(user3);
//        userAnswersService.createUser(userAnswers3);
//
//    }
//
//    //for testing. Delete in production
//    @PostConstruct
//    public void updateUser() {
//
//        user1 = userService.findFirstUserByName("user6");
//        user1.setEmail("email6wwwww");
//
//        userService.updateUser(user1);
//
//    }
//
//
//    @PostConstruct()
//    public void userMatch() {
//
//        UserMatch userMatch = userMatchService.match(user1,user2, MatchMethod.PEARSONCORRELATION);
//        userMatchService.createUser(userMatch);
//
//        UserMatch userMatch1 = userMatchService.match(user1, user2, MatchMethod.PEARSONCORRELATION);
//        userMatchService.createUser(userMatch1);
//        }
//
//
//
//    @PostConstruct()
//    public void userMatch2() {
//
//        User user1 = userService.findUserByNameOrEmail("email7");
//        User user2 = userService.findUserByNameOrEmail("email8");
//
//        UserMatch userMatch = userMatchService.match(user1,user2, MatchMethod.PEARSONCORRELATION);
//        userMatchService.createUser(userMatch);
//        }
//
//    @PostConstruct
//    public void createSecondUserAnswers() {
//        User user1 = userService.findUserByNameOrEmail("email7");
//        User user2 = userService.findUserByNameOrEmail("email8");
//
//        UserAnswers userAnswers1 = Entity.createRandomUserAnswers(user1);
//        userAnswersService.createUser(userAnswers1);
//
//        UserAnswers userAnswers2 = Entity.createRandomUserAnswers(user2);
//        userAnswersService.createUser(userAnswers2);
//
//        List<UserAnswers> userAnswersSet1 = userAnswersService.findAllUserAnswersByUser_IdOrderByPassDateDesc(user1.getId());
//        List<UserAnswers> userAnswersSet3 = userAnswersService.findAllUserAnswersByUser_IdOrderByPassDateDesc(user2.getId());
//
//        UserAnswersDtoConverter userAnswersDtoConverter = new UserAnswersDtoConverter(env);
//        List<UserAnswersDto> userAnswersDtos1 = userAnswersDtoConverter.transform(userAnswersSet1);
//        List<UserAnswersDto> userAnswersDtos2 = userAnswersDtoConverter.transform(userAnswersSet3);
//
//
//        System.out.println("+++++++++++++++++++++++++++++++++");
//        userAnswersDtos1.forEach(System.out::println);
//        System.out.println("+++++++++++++++++++++++++++++++++");
//        userAnswersDtos2.forEach(System.out::println);
//
//    }

}
