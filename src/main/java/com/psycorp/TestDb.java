package com.psycorp;

import com.psycorp.model.entity.User;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueCompatibilityAnswersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
//@PropertySource(value = {"classpath:testing/scalesquestionsukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
public class TestDb {

//    private final Environment env;
//    private final ValueCompatibilityAnswersService userAnswersService;
//    private final UserService userService;
//    private final UserAccountService userAccountService;
//    private final UserMatchService userMatchService;
//    private Random random = new Random();
//
//    @Autowired
//    public TestDb(Environment env,
//                  ValueCompatibilityAnswersService userAnswersService,
//                  UserService userService,
//                  UserAccountService userAccountService,
//                  UserMatchService userMatchService) {
//        this.env = env;
//        this.userAnswersService = userAnswersService;
//        this.userService = userService;
//        this.userAccountService = userAccountService;
//        this.userMatchService = userMatchService;
//    }
////
//////    private User user1 = new User();
//////    private User user2 = new User();
//////    private User user3 = new User();
//////
//////
//
//    //for testing. Delete in production
//    @PostConstruct
//    public void createInitEntities() {
//        String username = "Kate";
//
//        User user1 = new User();
//        user1.setName(username);
//        user1.setEmail(username.toLowerCase() + "@gmail.com");
//        user1.setAge(getRandomAge());
//        user1 = userService.createUser(user1);
////
////        ValueCompatibilityAnswersEntity userAnswers1 = Entity.createRandomUserAnswers(user1);
////        userAnswersService.createUser(userAnswers1);
////
//        User user2 = new User();
//        user2.setName("user23");
//        user2.setEmail("email7");
//        user2 = userService.createUser(user2);
////
////        ValueCompatibilityAnswersEntity userAnswers2 = Entity.createRandomUserAnswers(user2);
////        userAnswersService.createUser(userAnswers2);
////
////
////        user3.setName("user8");
////        user3.setEmail("email8");
////        user3 = userService.createUser(user3);
////
////        ValueCompatibilityAnswersEntity userAnswers3 = Entity.createRandomUserAnswers(user3);
////        userAnswersService.createUser(userAnswers3);
////
//    }

//    public Integer getRandomAge() {
//        return random.nextInt((75 - 15) + 1) + 15;
//    }
////
////    //for testing. Delete in production
////    @PostConstruct
////    public void updateUser() {
////
////        user1 = userService.findFirstUserByName("user6");
////        user1.setEmail("email6wwwww");
////
////        userService.updateUser(user1);
////
////    }
////
////
////    @PostConstruct()
////    public void userMatch() {
////
////        UserMatchEntity userMatch = userMatchService.match(user1,user2, MatchMethod.PEARSONCORRELATION);
////        userMatchService.createUser(userMatch);
////
////        UserMatchEntity userMatch1 = userMatchService.match(user1, user2, MatchMethod.PEARSONCORRELATION);
////        userMatchService.createUser(userMatch1);
////        }
////
////
////
////    @PostConstruct()
////    public void userMatch2() {
////
////        User user1 = userService.findUserByNameOrEmail("email7");
////        User user2 = userService.findUserByNameOrEmail("email8");
////
////        UserMatchEntity userMatch = userMatchService.match(user1,user2, MatchMethod.PEARSONCORRELATION);
////        userMatchService.createUser(userMatch);
////        }
////
////    @PostConstruct
////    public void createSecondUserAnswers() {
////        User user1 = userService.findUserByNameOrEmail("email7");
////        User user2 = userService.findUserByNameOrEmail("email8");
////
////        ValueCompatibilityAnswersEntity userAnswers1 = Entity.createRandomUserAnswers(user1);
////        userAnswersService.createUser(userAnswers1);
////
////        ValueCompatibilityAnswersEntity userAnswers2 = Entity.createRandomUserAnswers(user2);
////        userAnswersService.createUser(userAnswers2);
////
////        List<ValueCompatibilityAnswersEntity> userAnswersSet1 = userAnswersService.findAllUserAnswersByUser_IdOrderByPassDateDesc(user1.getId());
////        List<ValueCompatibilityAnswersEntity> userAnswersSet3 = userAnswersService.findAllUserAnswersByUser_IdOrderByPassDateDesc(user2.getId());
////
////        UserAnswersDtoConverter userAnswersDtoConverter = new UserAnswersDtoConverter(env);
////        List<ValueCompatibilityAnswersDto> userAnswersDtos1 = userAnswersDtoConverter.transform(userAnswersSet1);
////        List<ValueCompatibilityAnswersDto> userAnswersDtos2 = userAnswersDtoConverter.transform(userAnswersSet3);
////
////
////        System.out.println("+++++++++++++++++++++++++++++++++");
////        userAnswersDtos1.forEach(System.out::println);
////        System.out.println("+++++++++++++++++++++++++++++++++");
////        userAnswersDtos2.forEach(System.out::println);
////
////    }

}
