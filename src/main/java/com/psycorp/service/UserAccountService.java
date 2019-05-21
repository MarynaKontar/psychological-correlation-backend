package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.objects.UserAccount;

import java.util.List;

public interface UserAccountService {

    List<UserAccount> getAll();

    UserAccountEntity insert(User user);

    UserAccount getUserAccount(User user);

    UserAccount update(UserAccount userAccount);

    UserAccount inviteForMatching(UserAccount userAccount);
}
