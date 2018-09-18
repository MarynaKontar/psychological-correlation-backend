package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.util.AuthUtil;
import com.psycorp.service.UserService;
import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//@Primary
//@Scope("singleton")//default бины создаются синглтонами
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAnswersRepository userAnswersRepository;
    private final UserMatchRepository userMatchRepository;
    private final AuthUtil authUtil;
    private final Environment env;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserAnswersRepository userAnswersRepository
            , UserMatchRepository userMatchRepository, AuthUtil serviceUtil, Environment env) {
        this.userRepository = userRepository;
        this.userAnswersRepository = userAnswersRepository;
        this.userMatchRepository = userMatchRepository;
        this.authUtil = serviceUtil;
        this.env = env;
    }

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public User createUser(User user) {
        return userRepository.insert(user);
    }

    @Override
    public User findFirstUserByEmail(String email) {
        return userRepository.findFirstByEmail(email)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFind") + " for email: " + email));
    }

    @Override
    public User findFirstUserByName(String name) {
        return userRepository.findFirstByName(name)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFind") + " for user name: " + name));
    }

    @Override
    public User findById(ObjectId userId) {
        authUtil.userAuthorization(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFind") + " for user id: " + userId));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User deleteUser(ObjectId userId) {

        authUtil.userAuthorization(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFind") + " for user id: " + userId));
        userAnswersRepository.removeAllByUserId(userId);
        userMatchRepository.removeAllByUserId(userId);
        userRepository.delete(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

}
