package com.psycorp.service.implementation;

import com.psycorp.model.entity.User;
import com.psycorp.repository.UserRepository;
import com.psycorp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public User insert(User user) {
        return userRepository.insert(user);
    }

    @Override
    public User findFirstUserByEmail(String email) {
        return userRepository.findFirstByEmail(email);
    }
}
