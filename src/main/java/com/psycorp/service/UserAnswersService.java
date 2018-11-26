package com.psycorp.service;

import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserAnswersService {

    UserAnswersEntity getLastPassedTest(User user);

    UserAnswersEntity getInitUserAnswers();

//    UserAnswersEntity save(UserAnswersEntity userAnswersEntity);
    UserAnswersEntity saveChoices(String token, UserAnswersEntity userAnswersEntity, List<Choice> choices, Area area);

    UserAnswersEntity findById(ObjectId id);
    UserAnswersEntity findLastUserAnswersByUserNameOrEmail(String userName);
    List<UserAnswersEntity> findAllByUserNameOrderByCreationDateDesc(String userName);

    UserAnswersEntity getLastPassedTest();
}
