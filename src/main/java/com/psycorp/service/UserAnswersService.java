package com.psycorp.service;

import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;
import org.bson.types.ObjectId;

import java.security.Principal;
import java.util.List;

public interface UserAnswersService {

    UserAnswers getInitUserAnswers();

    UserAnswers save(UserAnswers userAnswers);
    UserAnswers saveChoices(List<Choice> choices, Principal principal, String userName);

    UserAnswers findById(ObjectId id);
    UserAnswers findLastUserAnswersByUserName(String userName);
    List<UserAnswers> findAllByUserNameOrderByCreationDateDesc(String userName);

    void validateArea(List<Choice> choices, Area area);
}
