package com.psycorp.service.implementation;

import com.psycorp.model.entity.User;
import com.psycorp.repository.UserRepository;
import com.psycorp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
//@Primary
//@Scope("singleton")//default бины создаются синглтонами
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public User insert(User user) {
        return userRepository.insert(user);
    }

    @Override
    public User update(User user) {

        return userRepository.save(user); }

    @Override
    public User changeUserName(User user, Principal principal, String userName) {

        //TODO сделать Transactional (try-catch-finally).  Или поставить @Transactional, если она появится в след. релизе для MongoDb
        //TODO поменять userName в каждом документе коллекций userAnswers и userMatch, в котором userName было principal.getName()
//        if(principal == null || !principal.getName().equals(user.getName())){
            user = userRepository.insert(user);
//            userRepository.removeByName(principal.getName());
            userRepository.removeByName(userName);
//        } else user = userRepository.save(user);

        return user;
    }

    @Override
    public User delete(String userName) {
        User user = userRepository.findFirstByName(userName);
        //TODO после введения security, поменять сигнатуру метода и передавать Principal principal
        //TODO сделать Transactional (try-catch-finally).
        //TODO удалить каждый документ в коллекций userAnswers и userMatch, в котором userName было principal.getName()
        userRepository.removeByName(userName);
        return user;
    }

    @Override
    public User findFirstUserByEmail(String email) {
        return userRepository.findFirstByEmail(email);
    }

    @Override
    public User findFirstUserByName(String name) {
        return userRepository.findFirstByName(name);
    }

}
