package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
public class UserMatch extends AbstractEntity{
    private ObjectId id;
    @NotNull @Valid
    private Set<User> users;
    @NotNull @Valid
    private List<Matching> matches;
}
