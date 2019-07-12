package com.psycorp.service;

import com.psycorp.model.dto.SomeDto;
import com.psycorp.model.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

public interface UserService {
//    User createUser(User user);

    User createAnonimUser();

    User find(User user);
    User findUserByNameOrEmail(String email);
    User findFirstUserByName(String name);
    User findById (ObjectId userId);
    User getPrincipalUser();

//    List<User> getUsersForMatching();

    //TODO deleteUser in production
    List<User> findAll();

    User updateUser(User user);

    User addNewUsersForMatching(User user, List<User> userForMatching, Update.Position position);

    User deleteUser(ObjectId userId);

    User addNameAgeAndGender(User user);

    void checkIfUsernameOrEmailExist(String nameOrEmail);

    List<SomeDto> getVCAnswersWithUserInfo();
}
