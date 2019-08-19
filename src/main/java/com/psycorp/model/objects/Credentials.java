package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bson.types.ObjectId;

/**
 * Object data level for collecting and transforming data from a database.
 * Credentials collecting from {@link com.psycorp.model.security.CredentialsEntity}.
 * This class with {@link com.psycorp.model.security.CredentialsEntity},
 * {@link com.psycorp.model.dto.CredentialsDto},
 * {@link com.psycorp.model.dto.ChangePasswordDto},
 * {@link com.psycorp.model.dto.UsernamePasswordDto} hold the password!!!
 * @author Maryna Kontar
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"password"})
public class Credentials extends AbstractEntity {
    private ObjectId id;
    private String password;
    private User user;
}
