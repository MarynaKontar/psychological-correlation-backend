package com.psycorp.model.dto;

import com.psycorp.model.enums.Gender;
import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) data level
 * for hiding implementation details of {@link com.psycorp.model.objects.Credentials}.
 * <p>
 * Holds data that received from client for registration.
 * <p>
 * This class with {@link com.psycorp.model.security.CredentialsEntity},
 * {@link com.psycorp.model.objects.Credentials},
 * {@link com.psycorp.model.dto.ChangePasswordDto},
 * {@link com.psycorp.model.dto.UsernamePasswordDto} hold the password!!!
 * @author Maryna Kontar
 */
@Data
@ToString(exclude = {"password"})
public class CredentialsDto extends AbstractDto{
    private ObjectId id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String password;
    @NotEmpty @Email
    private String email;

    // add this fields because they need for "full registration" (name, email, age, gender, password).
    // Don't use SimpleUserDto for this because need @NotEmpty name and email
    @NotNull
    private Gender gender;
    @NotNull
    private Integer age;
}
