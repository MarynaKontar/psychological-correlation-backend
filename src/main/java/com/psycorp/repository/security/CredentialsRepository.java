package com.psycorp.repository.security;

import com.psycorp.model.security.CredentialsEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Extension of {@link MongoRepository} for {@link CredentialsEntity}.
 * @author Maryna Kontar
 */
@Repository
public interface CredentialsRepository extends MongoRepository<CredentialsEntity, ObjectId> {

    Optional<CredentialsEntity> findByUserId(ObjectId objectId);
    Optional<CredentialsEntity> removeByUserId(ObjectId objectId);
}
