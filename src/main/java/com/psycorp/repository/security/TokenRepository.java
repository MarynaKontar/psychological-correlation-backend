package com.psycorp.repository.security;

import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Extension of {@link MongoRepository} for {@link TokenEntity}.
 * @author Maryna Kontar
 */
@Repository
public interface TokenRepository extends MongoRepository<TokenEntity, ObjectId> {
    Optional<TokenEntity> findByUserId(ObjectId userId);
    Optional<TokenEntity> findByToken(String token);
    Optional<TokenEntity> findByUserIdAndType(ObjectId userId, TokenType tokenType);

    void removeByUserId(ObjectId userId);
}
