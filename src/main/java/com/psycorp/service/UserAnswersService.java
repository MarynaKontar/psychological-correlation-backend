package com.psycorp.service;

import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.UserAnswers;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

public interface UserAnswersService {

    UserAnswers insert(UserAnswers userAnswers);

    UserAnswers findLastByUserName(String userName);

    List<UserAnswers> findAllByUserNameOrderByCreationDateDesc(String userName);

    UserAnswers getInitUserAnswers();

    List<Choice> choiceList();

}
