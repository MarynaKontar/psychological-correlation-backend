package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.objects.UserAccount;

public interface UserAccountService {

    UserAccount getUserAccount(User user);

    UserAccount update(UserAccount userAccount);
}
