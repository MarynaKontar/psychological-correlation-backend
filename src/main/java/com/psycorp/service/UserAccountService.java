package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.objects.UserAccount;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserAccountService {

    List<UserAccount> getAllRegisteredAndPassedTest();

    UserAccountEntity getUserAccountEntityByUserIdOrNull(ObjectId userId);

    UserAccountEntity insert(User user);

    UserAccountEntity getUserAccountEntityByUserId(ObjectId userId);

    UserAccount getUserAccount(User user);

    List<User> getUsersForMatching();

    UserAccount update(UserAccount userAccount);

    UserAccount inviteForMatching(UserAccount userAccount);
}
