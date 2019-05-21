package com.psycorp.model.security;

import com.psycorp.model.enums.TokenType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Data
@Document
public class TokenEntity {
    @Id
    private ObjectId id;
    private TokenType type;
    @Future
    @Indexed(expireAfterSeconds = 3600 * 24 * 7) // token удалится из базы данных через неделю после даты, указанной в expirationDate
    private LocalDateTime expirationDate;
    private String token;

    private ObjectId userId;

    //id of some user that invited this user
    private ObjectId whoInvitedUser;
}
