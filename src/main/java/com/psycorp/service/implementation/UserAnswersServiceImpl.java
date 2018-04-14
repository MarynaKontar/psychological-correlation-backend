package com.psycorp.service.implementation;

import com.psycorp.model.entity.UserAnswers;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.service.UserAnswersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAnswersServiceImpl implements UserAnswersService {

    @Autowired
    private UserAnswersRepository userAnswersRepository;

    @Override
    public UserAnswers insert(UserAnswers userAnswers){
        return userAnswersRepository.insert(userAnswers);
    }
}
