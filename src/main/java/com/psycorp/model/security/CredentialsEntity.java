package com.psycorp.model.security;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;


@Data
public class CredentialsEntity extends AbstractEntity {
    @Id
    private ObjectId id;
    private String password;
    @DBRef
    private User user;
}
