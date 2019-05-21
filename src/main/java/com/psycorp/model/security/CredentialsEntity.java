package com.psycorp.model.security;

import com.psycorp.model.entity.AbstractEntity;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;


@Data
public class CredentialsEntity extends AbstractEntity {
    @Id
    private ObjectId id;
    private String password;
    private ObjectId userId;
}
