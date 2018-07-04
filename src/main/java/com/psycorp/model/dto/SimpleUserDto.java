package com.psycorp.model.dto;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SimpleUserDto extends AbstractDto{
//    private ObjectId id;
    @NotEmpty
    private String name;
    @NotEmpty
    @Email
    private String email;
    private String password;
//    private Byte[] userPhoto;
}
