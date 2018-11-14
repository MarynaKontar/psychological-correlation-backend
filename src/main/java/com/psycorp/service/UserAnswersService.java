package com.psycorp.service;

import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.entity.ValueProfile;
import com.psycorp.model.enums.Area;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserAnswersService {

    UserAnswers getInitUserAnswers();

//    UserAnswers save(UserAnswers userAnswers);
    UserAnswers saveChoices(String token, UserAnswers userAnswers, List<Choice> choices, Area area);

    UserAnswers findById(ObjectId id);
    UserAnswers findLastUserAnswersByUserNameOrEmail(String userName);
    List<UserAnswers> findAllByUserNameOrderByCreationDateDesc(String userName);

    UserAnswers getLastPassedTest();

    ValueProfile getValueProfile(User noPrincipalUser);
}
