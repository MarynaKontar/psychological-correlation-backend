package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.objects.UserAccount;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for {@link UserAccountEntity} and {@link UserAccount}.
 * @author  Maryna Kontar
 */
public interface UserAccountService {

    List<UserAccount> getAllUserForMatchingPassedTest();

    Page<UserAccount> getAllRegisteredAndPassedTestPageable(Pageable pageable);

    UserAccountEntity getUserAccountEntityByUserIdOrNull(ObjectId userId);

    UserAccountEntity insert(User user);

    UserAccount getUserAccount(User user);

    List<User> getUsersForMatching(String userForMatchingToken);

    UserAccount update(UserAccount userAccount);

    UserAccount inviteForMatching(UserAccount userAccount);

}
