package com.psycorp.model.dto;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class SimpleUserDto extends AbstractDto{
//    private ObjectId id;
    private String userName;
    private String email;
//    private Byte[] userPhoto;
}
