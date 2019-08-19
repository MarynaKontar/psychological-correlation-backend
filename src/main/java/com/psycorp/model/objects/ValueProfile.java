package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.Result;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Scale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Object data level for collecting and transforming data from a database.
 * Contains calculated from {@link ValueCompatibilityAnswersEntity} results
 * of test for each {@link Scale} for given user.
 * @author Maryna Kontar
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
