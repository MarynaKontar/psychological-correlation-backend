package com.psycorp.model.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Document(collection = "user")
public class User extends AbstractEntity
{

    @Id
    private ObjectId id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String email;
}
