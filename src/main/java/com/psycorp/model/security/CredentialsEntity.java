package com.psycorp.model.security;

import com.psycorp.model.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

/**
 * Entity data level for saving data in database.
 * Stores credentials for given user with userId in database.
 * This class with {@link com.psycorp.model.objects.Credentials},
 * {@link com.psycorp.model.dto.CredentialsDto},
 * {@link com.psycorp.model.dto.ChangePasswordDto},
 * {@link com.psycorp.model.dto.UsernamePasswordDto} hold the password!!!
 * @author Maryna Kontar
 * @author Vitaliy Proskura
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"password"})
public class CredentialsEntity extends AbstractEntity {
    @Id
    private ObjectId id;
    private ObjectId userId;
    private String password;
}
