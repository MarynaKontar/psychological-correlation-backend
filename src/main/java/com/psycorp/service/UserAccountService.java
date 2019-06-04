package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.objects.UserAccount;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserAccountService {

    Page<UserAccount> getAllPageable(Pageable pageable);

    List<UserAccount> getAllRegisteredAndPassedTest();

    Page<UserAccount> getAllRegisteredAndPassedTestPageable(Pageable pageable);

    UserAccountEntity getUserAccountEntityByUserIdOrNull(ObjectId userId);

    UserAccountEntity insert(User user);

    UserAccountEntity getUserAccountEntityByUserId(ObjectId userId);

    UserAccount getUserAccount(User user);

    List<User> getUsersForMatching();

    UserAccount update(UserAccount userAccount);

    UserAccount inviteForMatching(UserAccount userAccount);

}
