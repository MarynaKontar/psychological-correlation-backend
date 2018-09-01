package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

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


    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserAnswersRepository userAnswersRepository
            , UserMatchRepository userMatchRepository) {
        this.userRepository = userRepository;
        this.userAnswersRepository = userAnswersRepository;
        this.userMatchRepository = userMatchRepository;
    }

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public User createUser(User user) {
        return userRepository.insert(user);
    }

    @Override
    public User findFirstUserByEmail(String email) {
        return userRepository.findFirstByEmail(email).orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Override
    public User findFirstUserByName(String name) {
        return userRepository.findFirstByName(name).orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Override
    public User findById(ObjectId userId) {
        return userRepository.findById(userId.toString()).orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User deleteUser(ObjectId userId) {
        User user = userRepository.findById(userId.toString()).orElseThrow(() -> new BadRequestException("User not found"));
        userAnswersRepository.removeAllByUser_Id(userId);
        userMatchRepository.removeAllByUserId(userId.toString());
        userRepository.delete(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

}
