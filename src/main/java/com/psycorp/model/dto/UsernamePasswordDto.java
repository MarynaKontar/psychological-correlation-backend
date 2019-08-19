package com.psycorp.model.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

/**
 * DTO (Data Transfer Object) data level.
 * Holds data that received from client for authentication.
 * <p>
 * This class with {@link com.psycorp.model.security.CredentialsEntity},
 * {@link com.psycorp.model.objects.Credentials},
 * {@link com.psycorp.model.dto.CredentialsDto},
 * {@link com.psycorp.model.dto.ChangePasswordDto} hold the password!!!
 * @author Maryna Kontar
 */
@Data
@ToString(exclude = {"password"})
public class UsernamePasswordDto {
    @NotEmpty private String name;
    @NotEmpty private String password;
}
