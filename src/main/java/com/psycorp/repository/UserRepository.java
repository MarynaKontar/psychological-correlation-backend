package com.psycorp.repository;

import com.psycorp.model.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Extension of {@link MongoRepository} for {@link User}.
 * @author Maryna Kontar
 */
@Repository
@RepositoryRestResource(collectionResourceRel = "user", path = "user")
public interface UserRepository extends MongoRepository<User, ObjectId> {

    //with one parameter isn't work, it's need all parameters
    Optional<User> findUserByNameOrEmail(String name, String email);

    List<User> findByUsersForMatchingIdContains(ObjectId userId);
}
