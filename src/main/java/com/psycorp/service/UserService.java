package com.psycorp.service;

import com.psycorp.model.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * Service interface for {@link User}.
 * @author  Maryna Kontar
 */
public interface UserService {

    User createAnonimUser();

    User find(User user);
    User findUserByNameOrEmail(String email);
    User findById (ObjectId userId);
    User getPrincipalUser();
    void checkIfUsernameOrEmailExist(String nameOrEmail);

    boolean checkIfExistById(ObjectId userId);

    User updateUser(User user);
    User addNewUsersForMatching(User user, List<User> userForMatching, Update.Position position);
    User addNameAgeAndGender(User user);

    User deleteUser(ObjectId userId);
}
