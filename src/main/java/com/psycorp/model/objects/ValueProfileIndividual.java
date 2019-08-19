package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.util.ValueProfileCommentUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Object data level for collecting and transforming data from a database.
 * Contains collecting and transform data from {@link ValueCompatibilityAnswersEntity}
 * and added to them {@link ValueProfileComment} for one user.
 * {@link ValueProfileComment} is added using {@link ValueProfileCommentUtil}.
 * @author Maryna Kontar
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValueProfileIndividual extends AbstractEntity {

    @NotNull @Valid
    private ValueProfile valueProfile;

    @NotNull
    private List<ValueProfileComment> valueProfileCommentList;
}
