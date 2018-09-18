package com.psycorp.repository.security;

import com.psycorp.model.security.TokenEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<TokenEntity, ObjectId> {
    Optional<TokenEntity> findByUser_Id(ObjectId userId);
    Optional<TokenEntity> findByToken(String token);
}
