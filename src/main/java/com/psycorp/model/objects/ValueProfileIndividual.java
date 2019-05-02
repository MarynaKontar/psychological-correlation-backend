package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ValueProfileIndividual extends AbstractEntity {

    @NotNull @Valid
    private ValueProfile valueProfile;

    @NotNull
    private List<ValueProfileComment> valueProfileCommentList;
}
