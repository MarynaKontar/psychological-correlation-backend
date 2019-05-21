package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class Credentials extends AbstractEntity {
    private ObjectId id;
    private String password;
    private User user;
}
