package com.psycorp.model.security;

import com.psycorp.model.enums.TokenType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

/**
 * Entity data level for saving data in database.
 * Stores token data for given user with userId in database.
 * @author Maryna Kontar
 * @author Vitaliy Proskura
 */
@Data
@Document
public class TokenEntity {
    @Id
    private ObjectId id;
    private ObjectId userId;
    private TokenType type;
    private String token;
    @Future
    @Indexed(expireAfterSeconds = 0) // token will be deleted from database (after week expireAfterSeconds = 3600 * 24 * 7) after date in expirationDate
    private LocalDateTime expirationDate;


}
