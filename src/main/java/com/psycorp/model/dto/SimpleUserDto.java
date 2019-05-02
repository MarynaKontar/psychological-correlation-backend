package com.psycorp.model.dto;

import com.psycorp.model.enums.Gender;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.Email;

@Data
public class SimpleUserDto extends AbstractDto{

    private ObjectId id;
    private String name;
    @Email
    private String email;
    private Integer age;
    private Gender gender;

}
