package com.psycorp.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * DTO (Data Transfer Object) data level.
 * <p>
 * Holds data that received from client for changing password.
 * <p>
 * This class with {@link com.psycorp.model.security.CredentialsEntity},
 * {@link com.psycorp.model.objects.Credentials},
 * {@link com.psycorp.model.dto.CredentialsDto},
 * {@link com.psycorp.model.dto.UsernamePasswordDto}  hold the password!!!
 * @author Maryna Kontar
 */
@Data
@ToString(exclude = {"oldPassword", "newPassword"})
public class ChangePasswordDto {
    String oldPassword;
    String newPassword;
}
