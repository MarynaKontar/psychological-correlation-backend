package com.psycorp.service;

import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import org.bson.types.ObjectId;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Map;

public interface UserAnswersService {

    UserAnswers getInitUserAnswers();

//    UserAnswers save(UserAnswers userAnswers);
    UserAnswers saveChoices(String token, UserAnswers userAnswers, List<Choice> choices, Area area);

    UserAnswers findById(ObjectId id);
    UserAnswers findLastUserAnswersByUserNameOrEmail(String userName);
    List<UserAnswers> findAllByUserNameOrderByCreationDateDesc(String userName);

    UserAnswers getLastPassedTest();

    Map<Scale, Double> getValueProfile();
}
