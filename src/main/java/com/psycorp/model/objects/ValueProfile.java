package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.Result;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.Scale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueProfile extends AbstractEntity {

    @NotNull @Valid
    private User user;
    @Valid
    private Map<Scale, Result> scaleResult;
    @NotNull
    private Boolean isPrincipalUser;
}
