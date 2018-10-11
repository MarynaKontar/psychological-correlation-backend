package com.psycorp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UsernamePasswordDto {
    @NotEmpty private String name;
    @NotEmpty private String password;
}