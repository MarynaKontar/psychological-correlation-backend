package com.psycorp.model.dto;

import com.psycorp.model.enums.Gender;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class CredentialsDto extends AbstractDto{
    private ObjectId id;
    @NotEmpty
    private String name;
    @NotEmpty
//    @Size(min = 8)
    private String password;
    @NotEmpty @Email
    private String email;

    // add this fields because they need for "full registration" (name, email, age, gender, password).
    // Don't use SimpleUserDto because need @NotEmpty name and email
    private Gender gender;
    private Integer age;
}
