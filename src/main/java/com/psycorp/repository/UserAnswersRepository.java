package com.psycorp.repository;

import com.psycorp.model.entity.UserAnswers;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RepositoryRestResource(collectionResourceRel = "userAnswers", path = "userAnswers")
public interface UserAnswersRepository extends MongoRepository<UserAnswers, ObjectId> {

    UserAnswers findByUser_Id(ObjectId userId);

    Set<UserAnswers> findAllByUser_IdOrderByPassDateDesc(ObjectId userId);
}
