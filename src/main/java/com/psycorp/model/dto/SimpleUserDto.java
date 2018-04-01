package com.psycorp.model.dto;

import lombok.Data;

@Data
public class SimpleUserDto {
    private Long id;
    private String userName;
    private Byte[] userPhoto;
}
