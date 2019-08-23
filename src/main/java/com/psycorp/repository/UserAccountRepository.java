package com.psycorp.repository;

import com.psycorp.model.entity.UserAccountEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Extension of {@link MongoRepository} for {@link UserAccountEntity}.
 * @author Maryna Kontar
 */
@Repository
public interface UserAccountRepository extends MongoRepository<UserAccountEntity, ObjectId>{

    Optional<UserAccountEntity> findByUserId(ObjectId userId);
}
