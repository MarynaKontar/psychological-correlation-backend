package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.util.UserMatchCommentUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
/**
 * Object data level for collecting and transforming data from a database.
 * Contains collecting and transform data from {@link ValueCompatibilityAnswersEntity}
 * and added to them {@link ValueProfileComment} for list of users.
 * {@link ValuesDifferencesComment} is added using {@link UserMatchCommentUtil}.
 * @author Maryna Kontar
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValueProfileMatching extends AbstractEntity {

    @NotNull @Valid
    private List<ValueProfile> valueProfileList;

    @NotNull @Valid
    private List<ValuesDifferencesComment> valuesDifferencesCommentList;
}
