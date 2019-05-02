package com.psycorp.repository;

import com.psycorp.model.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RepositoryRestResource(collectionResourceRel = "user", path = "user")
public interface UserRepository extends MongoRepository<User, ObjectId> {

    //with one parameter isn't work, it's need all parameters
    Optional<User> findUserByNameOrEmail(String name, String email);

    Optional<User> findFirstByEmail(String email);

    Optional<User> findFirstByName(String name);

}
