package com.psycorp.model.dto;

import lombok.Data;

@Data
public class ChangePasswordDto {
    String oldPassword;
    String newPassword;
}
