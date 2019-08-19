package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserMatchEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Object data level for collecting and transforming data from a database.
 * Contains collecting and transform data from {@link UserMatchEntity}
 * and added to them {@link UserMatchComment} in {@link Matching}.
 * @author Maryna Kontar
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserMatch extends AbstractEntity {
    private ObjectId id;
    @NotNull @Valid
    private Set<User> users;
    @NotNull @Valid
    private List<Matching> matches;
}
