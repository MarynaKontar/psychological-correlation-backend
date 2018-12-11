package com.psycorp.model.dto;

import com.psycorp.model.enums.Gender;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class SimpleUserDto extends AbstractDto{

    private ObjectId id;
//    @NotEmpty
    private String name;
//    @NotEmpty
    @Email
    private String email;
    private Integer age;
    private Gender gender;
}
