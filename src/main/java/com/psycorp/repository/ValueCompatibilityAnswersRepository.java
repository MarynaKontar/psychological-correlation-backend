package com.psycorp.repository;

import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Extension of {@link MongoRepository} for {@link ValueCompatibilityAnswersEntity}.
 * @author Maryna Kontar
 */
@Repository
@RepositoryRestResource(collectionResourceRel = "valueCompatibilityAnswersEntity", path = "valueCompatibilityAnswersEntity")
public interface ValueCompatibilityAnswersRepository extends MongoRepository<ValueCompatibilityAnswersEntity, ObjectId> {

    Optional<ValueCompatibilityAnswersEntity> findTopByUserIdOrderByPassDateDesc(ObjectId userId);
    Optional<ValueCompatibilityAnswersEntity> findTopByUserIdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);

    Boolean existsByUserIdAndPassed(ObjectId userId, Boolean passed);

    Optional<ValueCompatibilityAnswersEntity> removeByUserId (ObjectId userId);
}
