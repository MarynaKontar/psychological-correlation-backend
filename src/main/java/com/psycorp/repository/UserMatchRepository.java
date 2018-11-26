package com.psycorp.repository;

import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.enums.MatchMethod;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMatchRepository extends MongoRepository<UserMatchEntity, ObjectId>{

    @Query("{'users.$id': ?0}")
    List<UserMatchEntity> findByUserId(ObjectId userId);

    @Query("{'users.name': ?0}")
//    @Query("{'users': {'$ref':'user', '$name':{'$oname':?0}}}")
    List<UserMatchEntity> findByUserName(String userName);


    @Query("{'matches.matchMethod': ?0}")
    List<UserMatchEntity> findByMatchMethod(MatchMethod matchMethod);

    @Query("{'users.$id': ?0, 'matches.matchMethod': ?1}")
    List<UserMatchEntity> findByUserIdAndMatchMethod(ObjectId userId, MatchMethod matchMethod);

    @Query("{'users.name': ?0, 'matches.matchMethod': ?1}")
    List<UserMatchEntity> findByUserNameAndMatchMethod(String userName, MatchMethod matchMethod);

    @DeleteQuery("{'users.$id': ?0}")
    void removeAllByUserId(ObjectId userId);


}
