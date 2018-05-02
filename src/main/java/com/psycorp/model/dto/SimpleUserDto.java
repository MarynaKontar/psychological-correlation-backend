package com.psycorp.model.dto;

import lombok.Data;

@Data
public class SimpleUserDto extends AbstractDto{

    private String userName;
    private Byte[] userPhoto;
}
