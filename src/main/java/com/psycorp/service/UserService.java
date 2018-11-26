package com.psycorp.service;

import com.psycorp.model.entity.User;
import org.bson.types.ObjectId;

import java.security.Principal;
import java.util.List;

public interface UserService {
    User createUser(User user);

    User createAnonimUser();

    User find(User user);
    User findUserByNameOrEmail(String email);
    User findFirstUserByName(String name);
    User findById (ObjectId userId);
    User getPrincipalUser();
    //TODO deleteUser in production
    List<User> findAll();

    User updateUser(User user);

    User deleteUser(ObjectId userId);
}
