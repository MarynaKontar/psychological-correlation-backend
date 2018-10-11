package com.psycorp.repository.security;

import com.psycorp.model.security.CredentialsEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialsRepository extends MongoRepository<CredentialsEntity, ObjectId> {

    @Query("{'user.name': ?0}")
    CredentialsEntity findByUserName(String userName);
//    Optional<CredentialsEntity> findFirstByUser_Email(String name);

    Optional<CredentialsEntity> findByUser_Id(ObjectId objectId);
}
