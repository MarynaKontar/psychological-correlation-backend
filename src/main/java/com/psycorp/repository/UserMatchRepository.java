package com.psycorp.repository;

import com.psycorp.model.entity.UserMatch;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMatchRepository extends MongoRepository<UserMatch, ObjectId>{
}
