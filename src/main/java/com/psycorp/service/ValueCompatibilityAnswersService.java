package com.psycorp.service;

import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import org.bson.types.ObjectId;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ValueCompatibilityAnswersService {

    ValueCompatibilityAnswersEntity getLastPassedTest(User user);

    Boolean ifTestPassed(ObjectId userId);

    ValueCompatibilityAnswersEntity getInitValueCompatibilityAnswers();

    @Transactional
    ValueCompatibilityAnswersEntity saveFirstPartOfTests(String token, String userForMatchingToken,
                                                         ValueCompatibilityAnswersEntity answersEntity,
                                                         List<Choice> choices, Area area);

    //    ValueCompatibilityAnswersEntity save(ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity);
    ValueCompatibilityAnswersEntity saveChoices(String token, String userForMatchingToken,
                                                ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity,
                                                List<Choice> choices, Area area);

    ValueCompatibilityAnswersEntity findById(ObjectId id);
    ValueCompatibilityAnswersEntity findLastValueCompatibilityAnswersByUserNameOrEmail(String userName);
    List<ValueCompatibilityAnswersEntity> findAllByUserNameOrderByCreationDateDesc(String userName);

    ValueCompatibilityAnswersEntity getLastPassedTest();

}
