package com.psycorp.model.entity;

import com.psycorp.model.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@Document(collection = "user")
public class User extends AbstractEntity
{

    @Id
    private ObjectId id;
//    @NotEmpty
//    @Indexed(unique = true)
    private String name;
//    @NotEmpty
//    @Email @Indexed(unique = true)
    private String email;
    private UserRole role;

    public User(String name, UserRole role) {
        this.name = name;
        this.role = role;
    }
}
