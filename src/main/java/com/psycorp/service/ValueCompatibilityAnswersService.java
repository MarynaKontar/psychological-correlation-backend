package com.psycorp.service;

import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import org.bson.types.ObjectId;

import java.util.List;

public interface ValueCompatibilityAnswersService {

    ValueCompatibilityAnswersEntity getLastPassedTest(User user);

    Boolean ifTestPassed(User user);

    ValueCompatibilityAnswersEntity getInitValueCompatibilityAnswers();

//    ValueCompatibilityAnswersEntity save(ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity);
    ValueCompatibilityAnswersEntity saveChoices(String token, ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity, List<Choice> choices, Area area);

    ValueCompatibilityAnswersEntity findById(ObjectId id);
    ValueCompatibilityAnswersEntity findLastValueCompatibilityAnswersByUserNameOrEmail(String userName);
    List<ValueCompatibilityAnswersEntity> findAllByUserNameOrderByCreationDateDesc(String userName);

    ValueCompatibilityAnswersEntity getLastPassedTest();

    void changeInviteTokenToAccess(String token);
}
