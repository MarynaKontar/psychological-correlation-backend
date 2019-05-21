package com.psycorp.model.entity;

import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.UserRole;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import java.util.List;

@Data
@Document(collection = "user")
public class User extends AbstractEntity {

    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private String name;
    @Email @Indexed(sparse = true, unique = true)
    private String email;
    private Integer age;
    private Gender gender;
    private UserRole role;
    private List<ObjectId> usersForMatchingId;
}
