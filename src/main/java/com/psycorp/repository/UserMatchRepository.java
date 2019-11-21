package com.psycorp.repository;

import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.enums.MatchMethod;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Extension of {@link MongoRepository} for {@link UserMatchEntity}.
 * @author Maryna Kontar
 */
@Repository
public interface UserMatchRepository extends MongoRepository<UserMatchEntity, ObjectId>{

    List<UserMatchEntity> findByUsersIdContaining(ObjectId userId);

    @Query("{'matches.matchMethod': ?0}")
    List<UserMatchEntity> findByMatchMethod(MatchMethod matchMethod);

    @Query("{'usersId': ?0, 'matches.matchMethod': ?1}")
    List<UserMatchEntity> findByUsersIdContainingAndMatchMethod(ObjectId userId, MatchMethod matchMethod);

    void removeByUsersIdContaining (ObjectId userId);

}
