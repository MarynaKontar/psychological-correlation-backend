package com.psycorp.service;

import com.psycorp.model.entity.User;

import java.security.Principal;

public interface UserService {
    User insert(User user);

    User update(User user);

//    User changeUserName(User user, Principal principal);
    User changeUserName(User user, Principal principal, String userName);

    User delete(String userName);

    User findFirstUserByEmail(String email);

    User findFirstUserByName(String name);
}
