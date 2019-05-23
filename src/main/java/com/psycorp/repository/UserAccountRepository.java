package com.psycorp.repository;

import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.objects.UserAccount;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends MongoRepository<UserAccountEntity, ObjectId>{

    Optional<UserAccountEntity> findByUserId(ObjectId userId);
}
