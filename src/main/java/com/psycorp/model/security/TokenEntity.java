package com.psycorp.model.security;

import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class TokenEntity {
    @Id
    private ObjectId id;
    private TokenType type;
    @Future
    @Indexed(expireAfterSeconds = 3600 * 24 * 7) // token удалится из базы данных через неделю после даты, указанной в expirationDate (т.е. через 2 недели после создания)
    private LocalDateTime expirationDate;
    private String token;
    @DBRef
    private User user;

    //id of some user that invited this user
    private ObjectId whoInvitedUser;
}
