package com.psycorp.model.dto;

import com.psycorp.model.entity.User;
import com.psycorp.model.enums.Gender;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.constraints.Email;

/**
 * DTO (Data Transfer Object) data level
 * for hiding implementation details of {@link User}.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Data
public class SimpleUserDto extends AbstractDto{

    private ObjectId id;
    private String name;
    @Email
    private String email;
    private Integer age;
    private Gender gender;

}
