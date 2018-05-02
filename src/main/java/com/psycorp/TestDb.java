package com.psycorp;

import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.entity.UserMatch;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.util.Entity;
import com.psycorp.сonverter.UserAnswersDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Component
public class TestDb {

    private final UserAnswersService userAnswersService;
    private final UserService userService;
    private final UserMatchService userMatchService;

    //for testing. Delete in production
    private User user1 = new User();
    private User user2 = new User();


    @Autowired
    public TestDb(UserAnswersService userAnswersService, UserService userService,
                             UserMatchService userMatchService) {
        this.userAnswersService = userAnswersService;
        this.userService = userService;
        this.userMatchService = userMatchService;
    }


    //for testing. Delete in production
//    @PostConstruct
//    public void createInitEntities() {
//
//        user1.setName("user5");
//        user1.setEmail("email5");
//        user1 = userService.insert(user1);
//
//        UserAnswers userAnswers1 = Entity.createUserAnswers(user1);
//        userAnswersService.insert(userAnswers1);
//
//
//        user2.setName("user6");
//        user2.setEmail("email6");
//        user2 = userService.insert(user2);
//
//        UserAnswers userAnswers2 = Entity.createUserAnswers(user2);
//        userAnswersService.insert(userAnswers2);
//
//    }
//
//    @PostConstruct()
//    public void userMatch() {
//
//        UserMatch userMatch = new UserMatch();
//
//        userMatch.setUserOne(user1);
//        userMatch.setUserTwo(user2);
//        userMatch.setGoalMatch(userMatchService.goalMatch(user1, user2));
//        userMatch.setQualityMatch(userMatchService.qualityMatch(user1, user2));
//        userMatch.setStateMatch(userMatchService.stateMatch(user1, user2));
//        userMatch.setTotalMatch(userMatchService.totalMatch(user1, user2));
//
//        userMatchService.insert(userMatch);
//
//        }
//
//
//
//    @PostConstruct()
//    public void userMatch2() {
//
//        UserMatch userMatch = new UserMatch();
//        User user1 = userService.findFirstUserByEmail("email5");
//        User user2 = userService.findFirstUserByEmail("email6");
//
//        userMatch.setUserOne(user1);
//        userMatch.setUserTwo(user2);
//        userMatch.setGoalMatch(userMatchService.goalMatch(user1, user2));
//        userMatch.setQualityMatch(userMatchService.qualityMatch(user1, user2));
//        userMatch.setStateMatch(userMatchService.stateMatch(user1, user2));
//        userMatch.setTotalMatch(userMatchService.totalMatch(user1, user2));
//
//        userMatchService.insert(userMatch);
//
//        }

    @PostConstruct
    public void createSecondUserAnswers() {
        User user1 = userService.findFirstUserByEmail("email2");
        User user2 = userService.findFirstUserByEmail("email3");

//        UserAnswers userAnswers1 = Entity.createUserAnswers(user1);
//        userAnswersService.insert(userAnswers1);
//
//        UserAnswers userAnswers2 = Entity.createUserAnswers(user3);
//        userAnswersService.insert(userAnswers2);

        Set<UserAnswers> userAnswersSet1 = userAnswersService.findAllUserAnswersByUser_IdOrderByPassDateDesc(user1.getId());
        Set<UserAnswers> userAnswersSet3 = userAnswersService.findAllUserAnswersByUser_IdOrderByPassDateDesc(user2.getId());

        UserAnswersDtoConverter userAnswersDtoConverter = new UserAnswersDtoConverter();
        List<UserAnswersDto> userAnswersDtos1 = userAnswersDtoConverter.transform(userAnswersSet1);
        List<UserAnswersDto> userAnswersDtos2 = userAnswersDtoConverter.transform(userAnswersSet3);


        System.out.println("+++++++++++++++++++++++++++++++++");
        userAnswersDtos1.forEach(System.out::println);
        System.out.println("+++++++++++++++++++++++++++++++++");
        userAnswersDtos2.forEach(System.out::println);

    }

}
