package com.psycorp.model.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "user")
public class User extends AbstractEntity{
    private String name;
    private String email;
}
