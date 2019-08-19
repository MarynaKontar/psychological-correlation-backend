package com.psycorp.model.entity;

import com.psycorp.model.enums.Gender;
import com.psycorp.model.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import java.util.List;

/**
 * Entity data level for saving data in database.
 * Stores user data in database.
 * @author Maryna Kontar
 * @author Vitaliy Proskura
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
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

    // cannot move to UserAccountEntity because this field is necessary even
    // if the user does not have a user account (UserRole.ANONIM) at the request of the customer,
    // compare the test results of unregistered users
    private List<ObjectId> usersForMatchingId;
}
