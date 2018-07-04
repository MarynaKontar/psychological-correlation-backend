package com.psycorp.repository;

import com.psycorp.model.entity.UserMatch;
import com.psycorp.model.enums.MatchMethod;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMatchRepository extends MongoRepository<UserMatch, ObjectId>{

    @Query("{'users.$id': ?0}")
    List<UserMatch> findByUserName(String userName);

    @Query("{'matches.matchMethod': ?0}")
    List<UserMatch> findByMatchMethod(MatchMethod matchMethod);

    @Query("{'users.$id': ?0, 'matches.matchMethod': ?1}")
    List<UserMatch> findByUserNameAndMatchMethod(String userName, MatchMethod matchMethod);

    @DeleteQuery("{'users.$id': ?0}")
    void removeAllByUserName(String userName);

}
