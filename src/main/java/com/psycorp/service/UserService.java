package com.psycorp.service;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * Service interface for {@link User}.
 * @author Maryna Kontar
 */
public interface UserService {

    /**
     * Creates anonim user with role ANONIM, without password.
     * @return created user.
     */
    User createAnonimUser();

    /**
     * Saves name, age and gender to principal user.
     * If in user will be an email, it will not be saved. User role isn't changed.
     * @param user that contain incomplete user information (name, age and gender), must not be {@literal null}.
     * @return updated user
     * @throws BadRequestException if user or user name, age or gender are {@literal null}
     */
    User addNameAgeAndGender(User user);

    /**
     * Adds ids of all users from usersForMatching to field usersForMatchingId of user at position.
     * @param user user to whom we are adding ids to field usersForMatchingId at position, must not be {@literal null}.
     * @param usersForMatching users whose ids we are adding to user, must not be {@literal null}.
     * @param position the position to which we will add ids, must not be {@literal null}.
     * @return updated user.
     */
    User addNewUsersForMatching(User user, List<User> usersForMatching, Update.Position position);

    /**
     * Finds user.
     * @param user the user by whose id the user is taken from the database.
     * @return user from database, must not be {@literal null}.
     * @throws BadRequestException if user or user id is {@literal null}.
     */
    User find(User user);

    /**
     * Finds user by userId.
     * @param userId the id of the user, must not be {@literal null}..
     * @return user from database.
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     * @throws BadRequestException if none found.
     */
    User findById (ObjectId userId);

    /**
     * Finds user by name or email {@code nameOrEmail}.
     * @param nameOrEmail name or email of user, must not be {@literal null}.
     * @return user from database.
     * @throws BadRequestException if nameOrEmail is {@literal null} or none found.
     */
    User findUserByNameOrEmail(String nameOrEmail);

    /**
     * Gets principal user.
     * @return principal user.
     * @throws AuthorizationException if none found.
     */
    User getPrincipalUser();

    /**
     * Checks whether an user with given name or email exists.
     * @param nameOrEmail must not be {@literal null}.
     * @throws BadRequestException if user with given name or email {@code nameOrEmail} exists
     * or if {@code nameOrEmail} is {@literal null}.
     */
    void checkIfUsernameOrEmailExist(String nameOrEmail);

    /**
     * Returns whether an user with the given id exists.
     * @param userId must not be {@literal null}.
     * @return {@literal true} if an user with the given id exists, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     */
    boolean checkIfExistById(ObjectId userId);

    /**
     * Updates fields role, name, age, gender and email for principal user.
     * Field role becomes USER.
     * @param user with information for updating, must not be {@literal null}.
     * @return updated principal user.
     * @throws BadRequestException if user is {@literal null}.
     */
    User updateUser(User user);

    /**
     * Deletes user from database. In addition to the user himself, deletes his account, tests,
     * and deletes his ID from other users.
     * @param userId id of the user to be deleted.
     * @return deleted user.
     */
    User deleteUser(ObjectId userId);
}
