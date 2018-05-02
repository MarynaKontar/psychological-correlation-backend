package com.psycorp.service;

import com.psycorp.model.entity.UserAnswers;
import org.bson.types.ObjectId;

import java.util.Set;

public interface UserAnswersService {

    UserAnswers insert(UserAnswers userAnswers);


    Set<UserAnswers> findAllUserAnswersByUser_IdOrderByPassDateDesc(ObjectId userId);
}
