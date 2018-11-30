package com.psycorp;

import com.psycorp.model.entity.User;
import com.psycorp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
//@PropertySource(value = {"classpath:testing/scalesquestionsukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
public class TestDb {

////    private final Environment env;
//
////    private final UserAnswersService userAnswersService;
//    private final UserService userService;
//
//    @Autowired
//    public TestDb(UserService userService) {
//        this.userService = userService;
//    }
////    private final UserMatchService userMatchService;
//
////    private User user1 = new User();
////    private User user2 = new User();
////    private User user3 = new User();
////
////
////    @Autowired
////    public TestDb(Environment env, UserAnswersService userAnswersService, UserService userService,
////                  UserMatchService userMatchService) {
////        this.env = env;
////        this.userAnswersService = userAnswersService;
////        this.userService = userService;
////        this.userMatchService = userMatchService;
////    }
//
//    //for testing. Delete in production
//    @PostConstruct
//    public void createInitEntities() {
//        User user1 = new User();
//        user1.setName("user22");
//        user1.setEmail("email6");
//        user1 = userService.createUser(user1);
////
////        UserAnswersEntity userAnswers1 = Entity.createRandomUserAnswers(user1);
////        userAnswersService.createUser(userAnswers1);
////
//        User user2 = new User();
//        user2.setName("user23");
//        user2.setEmail("email7");
//        user2 = userService.createUser(user2);
////
////        UserAnswersEntity userAnswers2 = Entity.createRandomUserAnswers(user2);
////        userAnswersService.createUser(userAnswers2);
////
////
////        user3.setName("user8");
////        user3.setEmail("email8");
////        user3 = userService.createUser(user3);
////
////        UserAnswersEntity userAnswers3 = Entity.createRandomUserAnswers(user3);
////        userAnswersService.createUser(userAnswers3);
////
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
////        UserAnswersEntity userAnswers1 = Entity.createRandomUserAnswers(user1);
////        userAnswersService.createUser(userAnswers1);
////
////        UserAnswersEntity userAnswers2 = Entity.createRandomUserAnswers(user2);
////        userAnswersService.createUser(userAnswers2);
////
////        List<UserAnswersEntity> userAnswersSet1 = userAnswersService.findAllUserAnswersByUser_IdOrderByPassDateDesc(user1.getId());
////        List<UserAnswersEntity> userAnswersSet3 = userAnswersService.findAllUserAnswersByUser_IdOrderByPassDateDesc(user2.getId());
////
////        UserAnswersDtoConverter userAnswersDtoConverter = new UserAnswersDtoConverter(env);
////        List<UserAnswersDto> userAnswersDtos1 = userAnswersDtoConverter.transform(userAnswersSet1);
////        List<UserAnswersDto> userAnswersDtos2 = userAnswersDtoConverter.transform(userAnswersSet3);
////
////
////        System.out.println("+++++++++++++++++++++++++++++++++");
////        userAnswersDtos1.forEach(System.out::println);
////        System.out.println("+++++++++++++++++++++++++++++++++");
////        userAnswersDtos2.forEach(System.out::println);
////
////    }

}
