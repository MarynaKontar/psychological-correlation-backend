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
 * @author Maryna Kontar
 */
public interface UserAccountService {

    /**
     * Insert new {@link UserAccountEntity} for saved in database user.
     * @param user must not be {@literal null}, must be saved in database.
     * @return inserted {@link UserAccountEntity}.
     */
    UserAccountEntity insert(User user);

    /**
     * Gives an user account for user, if there is, or create anonymous account without writing to the database.
     * @param user for whom the account is being getting
     * @return user account obtained by conversion UserAccountEntity from the database, if there is,
     * or created anonymous account without writing to the database
     */
    UserAccount getUserAccount(User user);

    /**
     * Gets list of {@link UserAccount} for all registered users from userForMatching of principal user
     * that passed value compatibility test.
     * @return list of {@link UserAccount}.
     */
    List<UserAccount> getAllUserForMatchingPassedTest();

    /**
     * Gets a {@link Page} of {@link UserAccount} for registered users,
     * that passed value compatibility test (except principal user)
     * meeting the paging restriction provided in the {@code Pageable} object.
     * @param pageable {@code Pageable} object that defines page options, must not be {@literal null}.
     * @return a page of {@link UserAccount} for registered users,
     * that passed value compatibility test (except principal user).
     */
    Page<UserAccount> getAllRegisteredAndPassedTestPageable(Pageable pageable);

    /**
     * Gets {@link UserAccountEntity} by userId user id.
     * @param userId must not be {@literal null}.
     * @return {@link UserAccountEntity} or {@literal null} if none exists.
     */
    UserAccountEntity getUserAccountEntityByUserIdOrNull(ObjectId userId);

    /**
     * Returns singleton list with user retrieves by userForMatchingToken
     * or list of usersForMatching for principal user if userForMatchingToken is {@literal null}.
     * @param userForMatchingToken
     * @return singleton list with user retrieves by userForMatchingToken or list of usersForMatching
     * for principal user if userForMatchingToken is {@literal null}.
     */
    List<User> getUsersForMatching(String userForMatchingToken);

    /**
     * Updates {@link UserAccount}.
     * @param userAccount must not be {@literal null}.
     * @return updated user account.
     */
    UserAccount update(UserAccount userAccount);

    /**
     * Adds principal user id to usersWhoInvitedYouId field of {@link UserAccountEntity} for userAccount
     * and id of user from userAccount to usersWhoYouInviteId field of {@link UserAccountEntity} for principal user.
     * @param userAccount must not be {@literal null}.
     * @return updated user account.
     */
    UserAccount inviteForMatching(UserAccount userAccount);

}
