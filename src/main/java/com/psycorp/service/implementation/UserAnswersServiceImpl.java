package com.psycorp.service.implementation;

import com.psycorp.model.entity.UserAnswers;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.service.UserAnswersService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserAnswersServiceImpl implements UserAnswersService {

    @Autowired
    private UserAnswersRepository userAnswersRepository;

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public UserAnswers insert(UserAnswers userAnswers){
        return userAnswersRepository.insert(userAnswers);
    }

    @Override
    public Set<UserAnswers> findAllUserAnswersByUser_IdOrderByPassDateDesc(ObjectId userId) {
        return userAnswersRepository.findAllByUser_IdOrderByPassDateDesc(userId);
    }
}
